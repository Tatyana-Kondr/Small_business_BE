package de.ait.smallBusiness_be.warehouse.services;

import de.ait.smallBusiness_be.customers.dao.CustomerRepository;
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
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@RequiredArgsConstructor
@Service
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseRecordRepository recordRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Transactional
    @Override
    public void recordOperation(Product product, TypeOfOperation type, BigDecimal qty, Long documentId, Customer partner, LocalDate date) {
        // 1. Добавляем запись в журнал движения
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

        // 2. Обновляем склад
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

    public WarehouseStockDto getStock(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
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
    public Page<WarehouseRecordDto> getProductHistory(Long productId, Pageable pageable) {
        Page<WarehouseRecord> history = recordRepository.findAllByProductIdOrderByDateDesc(productId, pageable);
        return history.map(record -> modelMapper.map(record, WarehouseRecordDto.class));
    }

    @Override
    public Page<WarehouseStockDto> getAllStocks(Pageable pageable) {
        return warehouseRepository.findAll(pageable)
                .map(stock -> modelMapper.map(stock, WarehouseStockDto.class));
    }
}
