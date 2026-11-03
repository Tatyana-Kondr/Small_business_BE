
package de.ait.smallBusiness_be.warehouse.services;

import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.products.dao.ProductRepository;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.purchases.model.TypeOfOperation;
import de.ait.smallBusiness_be.warehouse.dao.WarehouseRecordRepository;
import de.ait.smallBusiness_be.warehouse.dao.WarehouseRepository;
import de.ait.smallBusiness_be.warehouse.dto.WarehouseRecordDto;
import de.ait.smallBusiness_be.warehouse.dto.WarehouseStockDto;
import de.ait.smallBusiness_be.warehouse.models.Warehouse;
import de.ait.smallBusiness_be.warehouse.models.WarehouseRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseRecordRepository recordRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;


    @Override
    @Transactional
    public void recordOperation(Product product, TypeOfOperation type, BigDecimal qty, Long documentId, Customer partner, LocalDate date) {
        validateOperation(product, type, qty, documentId, date);

        WarehouseRecord record = WarehouseRecord.builder()
                .product(product)
                .typeOfOperation(type)
                .documentId(documentId)
                .quantity(qty)
                .date(date)
                .partner(partner)
                .partnerName(partner != null ? partner.getName() : null)
                .build();

        recordRepository.save(record);

        Warehouse warehouse = warehouseRepository.findByProduct(product)
                .orElseGet(() -> Warehouse.builder()
                        .product(product)
                        .quantity(BigDecimal.ZERO)
                        .build());

        BigDecimal newQty = switch (type) {
            case EINKAUF, KUNDENERSTATTUNG, PRODUKTION -> warehouse.getQuantity().add(qty);
            case VERKAUF, LIEFERANT_RABATT, PRODUKTIONSMATERIAL -> warehouse.getQuantity().subtract(qty);
        };

        warehouse.setQuantity(newQty);
        warehouseRepository.save(warehouse);
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseStockDto getStock(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RestApiException(ErrorDescription.PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND));

        Warehouse warehouse = warehouseRepository.findByProduct(product).orElse(null);

        if (warehouse == null) {
            return WarehouseStockDto.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(BigDecimal.ZERO)
                    .build();
        }

        return modelMapper.map(warehouse, WarehouseStockDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WarehouseRecordDto> getProductHistory(Long productId, Pageable pageable) {
        Page<WarehouseRecord> history = recordRepository.findAllByProductIdOrderByDateDesc(productId, pageable);

        return history.map(record -> modelMapper.map(record, WarehouseRecordDto.class));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WarehouseStockDto> getAllStocks(Pageable pageable) {
        return warehouseRepository.findAll(pageable)
                .map(stock -> modelMapper.map(stock, WarehouseStockDto.class));
    }

    @Override
    @Transactional
    public <T> void syncDocument(TypeOfOperation type, Long documentId, Customer partner, LocalDate date, List<T> items) {
        if (type == null || documentId == null || date == null || items == null) {
            throw new RestApiException(ErrorDescription.WAREHOUSE_INVALID_DOCUMENT, HttpStatus.BAD_REQUEST);
        }

        // Откатываем только движения данного вида документа
        rollbackDocument(documentId, getDocumentOperationTypes(type));

        // Создаём новые складские движения
        for (T item : items) {
            if (item == null) {
                throw new RestApiException(ErrorDescription.WAREHOUSE_INVALID_ITEM, HttpStatus.BAD_REQUEST);
            }

            Product product;
            BigDecimal qty;

            try {
                Method getProductMethod = item.getClass().getMethod("getProduct");
                Method getQuantityMethod = item.getClass().getMethod("getQuantity");
                product = (Product) getProductMethod.invoke(item);
                qty = (BigDecimal) getQuantityMethod.invoke(item);

            } catch (InvocationTargetException e) {
                log.error(
                        "Error invoking warehouse item getter: documentId={}, itemType={}",
                        documentId,
                        item.getClass().getName(),
                        e
                );

                throw new RestApiException(ErrorDescription.WAREHOUSE_ITEM_READ_FAILED, HttpStatus.INTERNAL_SERVER_ERROR
                );

            } catch (ReflectiveOperationException | ClassCastException e) {
                log.error(
                        "Failed to read warehouse document item: documentId={}, itemType={}",
                        documentId,
                        item.getClass().getName(),
                        e
                );

                throw new RestApiException(ErrorDescription.WAREHOUSE_ITEM_READ_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // Валидация параметров выполняется в recordOperation()
            recordOperation(product, type, qty, documentId, partner, date);
        }
    }

    @Override
    @Transactional
    public void rollbackDocument(Long documentId, Collection<TypeOfOperation> types) {
        if (documentId == null || types == null || types.isEmpty() || types.stream().anyMatch(t -> t == null)) {
            throw new RestApiException(ErrorDescription.WAREHOUSE_INVALID_DOCUMENT, HttpStatus.BAD_REQUEST);
        }

        List<WarehouseRecord> records = recordRepository.findAllByDocumentIdAndTypeOfOperationIn(documentId, types);

        if (records.isEmpty()) {
            return;
        }

        for (WarehouseRecord record : records) {

            Product product = record.getProduct();

            if (product == null
                    || record.getTypeOfOperation() == null
                    || record.getQuantity() == null
                    || record.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RestApiException(ErrorDescription.WAREHOUSE_INVALID_RECORD, HttpStatus.CONFLICT);
            }

            Warehouse warehouse = warehouseRepository.findByProduct(product)
                    .orElseThrow(() -> new RestApiException(ErrorDescription.WAREHOUSE_NOT_FOUND, HttpStatus.CONFLICT));

            BigDecimal qty = record.getQuantity();

            BigDecimal newQty = switch (record.getTypeOfOperation()) {
                case EINKAUF, KUNDENERSTATTUNG, PRODUKTION -> warehouse.getQuantity().subtract(qty);
                case VERKAUF, LIEFERANT_RABATT, PRODUKTIONSMATERIAL -> warehouse.getQuantity().add(qty);
            };

            warehouse.setQuantity(newQty);
            warehouseRepository.save(warehouse);
        }

        recordRepository.deleteAll(records);
    }


    private void validateOperation(Product product, TypeOfOperation type, BigDecimal qty, Long documentId, LocalDate date) {
        if (product == null) {
            throw new RestApiException(ErrorDescription.WAREHOUSE_INVALID_PRODUCT, HttpStatus.BAD_REQUEST);
        }

        if (type == null) {
            throw new RestApiException(ErrorDescription.WAREHOUSE_INVALID_OPERATION_TYPE, HttpStatus.BAD_REQUEST);
        }

        if (documentId == null || date == null) {
            throw new RestApiException(ErrorDescription.WAREHOUSE_INVALID_DOCUMENT, HttpStatus.BAD_REQUEST);
        }

        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RestApiException(ErrorDescription.WAREHOUSE_INVALID_QUANTITY, HttpStatus.BAD_REQUEST);
        }
    }

    private Collection<TypeOfOperation> getDocumentOperationTypes(
            TypeOfOperation type
    ) {
        return switch (type) {
            case EINKAUF, LIEFERANT_RABATT -> List.of(TypeOfOperation.EINKAUF, TypeOfOperation.LIEFERANT_RABATT);
            case VERKAUF, KUNDENERSTATTUNG -> List.of(TypeOfOperation.VERKAUF, TypeOfOperation.KUNDENERSTATTUNG);
            case PRODUKTION, PRODUKTIONSMATERIAL -> List.of(TypeOfOperation.PRODUKTION, TypeOfOperation.PRODUKTIONSMATERIAL);
        };
    }
}
