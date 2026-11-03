package de.ait.smallBusiness_be.purchases.services;

import de.ait.smallBusiness_be.customers.dao.CustomerRepository;
import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.payments.dao.PaymentRepository;
import de.ait.smallBusiness_be.payments.model.Payment;
import de.ait.smallBusiness_be.products.dao.ProductRepository;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.purchases.dao.PurchaseDocumentRepository;
import de.ait.smallBusiness_be.purchases.dao.PurchaseItemRepository;
import de.ait.smallBusiness_be.purchases.dao.PurchaseRepository;
import de.ait.smallBusiness_be.purchases.dao.TypeOfDocumentRepository;
import de.ait.smallBusiness_be.purchases.dto.NewPurchaseDto;
import de.ait.smallBusiness_be.purchases.dto.NewPurchaseItemDto;
import de.ait.smallBusiness_be.purchases.dto.PurchaseDto;
import de.ait.smallBusiness_be.purchases.model.*;
import de.ait.smallBusiness_be.warehouse.services.WarehouseService;
import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;


/**
 * 16.01.2025
 * SmB
 *
 * @author Kondratyeva (AIT TR)
 */

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final PurchaseDocumentRepository purchaseDocumentRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final TypeOfDocumentRepository typeOfDocumentRepository;
    private final WarehouseService warehouseService;
    private final PendingFileDeletionService pendingFileDeletionService;
    private final ModelMapper modelMapper;

    @Value("${file.document-dir}")
    private String documentDir;

    @Override
    @Transactional
    public PurchaseDto createPurchase(NewPurchaseDto newPurchaseDto) {

        Customer vendor = getVendorOrThrow(newPurchaseDto.getVendorId());
        TypeOfDocument document = getDocumentOrThrow(newPurchaseDto.getDocumentId());

        validatePurchaseItems(newPurchaseDto);

        Purchase purchase = modelMapper.map(newPurchaseDto, Purchase.class);

        purchase.setVendor(vendor);
        purchase.setDocument(document);
        purchase.setType(parseTypeOfOperation(newPurchaseDto.getType()));
        purchase.setPaymentStatus(PaymentStatus.OFFEN);

        calculatePurchaseAmounts(purchase, newPurchaseDto);

        Purchase savedPurchase = purchaseRepository.saveAndFlush(purchase);

        // Пересчитываем закупочную цену и дату последней закупки товара
        savedPurchase.getPurchaseItems().stream()
                .map(PurchaseItem::getProduct)
                .distinct()
                .forEach(this::recalculateProductPurchaseData);

        // Обновляем склад
        savedPurchase.getPurchaseItems().forEach(item -> {
            warehouseService.recordOperation(
                    item.getProduct(),
                    savedPurchase.getType(),
                    item.getQuantity(),
                    savedPurchase.getId(),
                    savedPurchase.getVendor(),
                    savedPurchase.getPurchasingDate()
            );
        });

        return toPurchaseDto(savedPurchase);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseDto> getAllPurchases(Pageable pageable) {
        return purchaseRepository.findAllWithSorting(pageable)
                .map(this::toPurchaseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseDto getPurchaseById(Long id) {
        Purchase purchase = getPurchaseOrThrow(id);
        return toPurchaseDto(purchase);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseDto> searchPurchases(Pageable pageable, String query) {
        return purchaseRepository.searchPurchases(pageable, query)
                .map(this::toPurchaseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseDto> getAllPurchasesByFilter(Pageable pageable, Long id, Long vendorId, String vendorName, Long documentId, String documentNumber, BigDecimal total, String paymentStatus, LocalDate startDate, LocalDate endDate, String searchQuery) {
        return purchaseRepository.filterByFields(pageable, id, vendorId, vendorName, documentId, documentNumber, total, paymentStatus, startDate, endDate, searchQuery)
                .map(this::toPurchaseDto);
    }

    @Override
    @Transactional
    public PurchaseDto updatePurchase(Long id, NewPurchaseDto newPurchaseDto) {

        Purchase purchase = getPurchaseOrThrow(id);
        validateVendorChange(purchase, newPurchaseDto.getVendorId());
        Customer vendor = getVendorOrThrow(newPurchaseDto.getVendorId());
        TypeOfDocument document = getDocumentOrThrow(newPurchaseDto.getDocumentId());

        validatePurchaseItems(newPurchaseDto);

        Set<Product> affectedProducts = purchase.getPurchaseItems()
                .stream()
                .map(PurchaseItem::getProduct)
                .collect(Collectors.toSet());

        purchase.setVendor(vendor);
        purchase.setDocument(document);
        purchase.setPurchasingDate(newPurchaseDto.getPurchasingDate());
        purchase.setDocumentNumber(newPurchaseDto.getDocumentNumber());
        purchase.setType(parseTypeOfOperation(newPurchaseDto.getType()));

        // Пересоздаём позиции и пересчитываем суммы Purchase
        calculatePurchaseAmounts(purchase, newPurchaseDto);

        Purchase updatedPurchase = purchaseRepository.saveAndFlush(purchase);

        recalculatePaymentStatus(updatedPurchase);

        // Добавляем товары из новой версии Purchase
        updatedPurchase.getPurchaseItems()
                .stream()
                .map(PurchaseItem::getProduct)
                .forEach(affectedProducts::add);

        // Пересчитываем закупочную цену и дату последней закупки
        // как для оставшихся/добавленных, так и для удалённых товаров
        affectedProducts.forEach(this::recalculateProductPurchaseData);

        // Синхронизируем документ со складом
        warehouseService.syncDocument(
                updatedPurchase.getType(),
                updatedPurchase.getId(),
                updatedPurchase.getVendor(),
                updatedPurchase.getPurchasingDate(),
                updatedPurchase.getPurchaseItems()
        );

        return toPurchaseDto(updatedPurchase);
    }

    @Override
    @Transactional
    public void deletePurchase(Long id) {

        Purchase purchase = getPurchaseOrThrow(id);

        // 1. Проверяем статус оплаты
        if (purchase.getPaymentStatus() == PaymentStatus.BEZAHLT) {
            throw new RestApiException(ErrorDescription.PURCHASE_ALREADY_PAID, HttpStatus.CONFLICT);
        }

        // 2. Проверяем наличие платежей
        if (paymentRepository.existsByPurchaseId(id)) {
            throw new RestApiException(ErrorDescription.PURCHASE_HAS_PAYMENTS, HttpStatus.CONFLICT);
        }

        // 3. Получаем URL документов покупки
        List<String> fileUrls = purchaseDocumentRepository
                .findAllByPurchaseIdOrderByIdAsc(id)
                .stream()
                .map(PurchaseDocument::getFileUrl)
                .toList();

        // 4. Запоминаем товары для пересчёта
        Set<Product> affectedProducts = purchase.getPurchaseItems()
                .stream()
                .map(PurchaseItem::getProduct)
                .collect(Collectors.toSet());

        // 5. Откатываем складские операции
        warehouseService.rollbackDocument(
                purchase.getId(),
                List.of(
                        TypeOfOperation.EINKAUF,
                        TypeOfOperation.LIEFERANT_RABATT
                )
        );

        // 6. Создаём задания на удаление файлов
        pendingFileDeletionService.createTasks(id, fileUrls);

        // 7. Удаляем покупку
        purchaseRepository.delete(purchase);
        purchaseRepository.flush();

        // 8. Пересчитываем данные товаров
        affectedProducts.forEach(this::recalculateProductPurchaseData);

        // 9. Запускаем очистку после успешного COMMIT
        if (!fileUrls.isEmpty()) {

            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {

                        @Override
                        public void afterCommit() {
                            try {
                                pendingFileDeletionService.processPendingFiles();
                            } catch (Exception e) {
                                // Purchase deletion has already been committed.
                                // Pending tasks remain available for scheduled retry.
                            }
                        }
                    }
            );
        }
    }

    @Override
    @Transactional
    public PurchaseDto updatePaymentStatus(Long purchaseId) {
        Purchase purchase = getPurchaseOrThrow(purchaseId);
        recalculatePaymentStatus(purchase);
        return toPurchaseDto(purchase);
    }


    private void recalculatePaymentStatus(Purchase purchase) {

        List<Payment> payments = paymentRepository.findByPurchaseId(purchase.getId());

        BigDecimal totalPaid = payments.stream()
                .map(payment -> switch (payment.getType()) {
                    case AUSGABE -> payment.getAmount();
                    case EINNAHME -> payment.getAmount().negate();
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmount = purchase.getTotal();

        if (totalPaid.compareTo(BigDecimal.ZERO) <= 0) {
            purchase.setPaymentStatus(PaymentStatus.OFFEN);

        } else if (totalPaid.compareTo(totalAmount) < 0) {
            purchase.setPaymentStatus(PaymentStatus.ANZAHLUNG);

        } else {
            purchase.setPaymentStatus(PaymentStatus.BEZAHLT);
        }
    }

    private void calculatePurchaseAmounts(
            Purchase purchase,
            NewPurchaseDto newPurchaseDto
    ) {
        purchase.getPurchaseItems().clear();

        int position = 1;

        for (NewPurchaseItemDto dto : newPurchaseDto.getPurchaseItems()) {
            PurchaseItem item = createPurchaseItem(purchase, dto, position++);

            purchase.getPurchaseItems().add(item);
        }

        BigDecimal subtotal = purchase.getPurchaseItems().stream()
                .map(PurchaseItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountAmount = purchase.getPurchaseItems().stream()
                .map(PurchaseItem::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxSum = purchase.getPurchaseItems().stream()
                .map(PurchaseItem::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = purchase.getPurchaseItems().stream()
                .map(PurchaseItem::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        purchase.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        purchase.setDiscountAmount(discountAmount.setScale(2, RoundingMode.HALF_UP));
        purchase.setTaxSum(taxSum.setScale(2, RoundingMode.HALF_UP));
        purchase.setTotal(total.setScale(2, RoundingMode.HALF_UP));
    }

    private PurchaseItem createPurchaseItem(Purchase purchase, NewPurchaseItemDto dto, int position) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.PRODUCT_NOT_FOUND.getDescription()));

        BigDecimal quantity = dto.getQuantity();
        BigDecimal unitPrice = dto.getUnitPrice();
        BigDecimal discount = dto.getDiscount();
        BigDecimal taxPercentage = dto.getTaxPercentage();

        // Стоимость позиции до скидки
        BigDecimal priceBeforeDiscount = unitPrice.multiply(quantity);

        // Сумма скидки
        BigDecimal discountAmount = priceBeforeDiscount
                .multiply(discount.movePointLeft(2))
                .setScale(2, RoundingMode.HALF_UP);

        // Netto после скидки
        BigDecimal totalPrice = priceBeforeDiscount
                .subtract(discountAmount)
                .setScale(2, RoundingMode.HALF_UP);

        // НДС после скидки
        BigDecimal taxAmount = totalPrice
                .multiply(taxPercentage.movePointLeft(2))
                .setScale(2, RoundingMode.HALF_UP);

        // Brutto
        BigDecimal totalAmount = totalPrice
                .add(taxAmount)
                .setScale(2, RoundingMode.HALF_UP);

        PurchaseItem item = new PurchaseItem();

        item.setPurchase(purchase);
        item.setProduct(product);
        item.setPosition(position);
        item.setProductName(dto.getProductName());
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setDiscount(discount);
        item.setDiscountAmount(discountAmount);
        item.setTotalPrice(totalPrice);
        item.setTaxPercentage(taxPercentage);
        item.setTaxAmount(taxAmount);
        item.setTotalAmount(totalAmount);

        return item;
    }

    private void recalculateProductPurchaseData(Product product) {

        List<PurchaseItem> latestItems = purchaseItemRepository.findLatestPurchaseItemsByProductId(product.getId());

        // У товара больше нет ни одной закупки
        if (latestItems.isEmpty()) {
            product.setPurchasingPrice(null);
            product.setDateOfLastPurchase(null);

            productRepository.save(product);
            return;
        }

        // Репозиторий уже сортирует позиции:
        // самая новая дата + самая высокая цена первой
        PurchaseItem latestItem = latestItems.get(0);

        BigDecimal purchasingPrice = latestItem.getUnitPrice();
        LocalDate purchasingDate = latestItem.getPurchase().getPurchasingDate();

        product.setPurchasingPrice(purchasingPrice);
        product.setDateOfLastPurchase(purchasingDate.atStartOfDay());

        // Продажную цену автоматически только повышаем
        BigDecimal calculatedSellingPrice = purchasingPrice
                .multiply(BigDecimal.valueOf(1.2))
                .setScale(2, RoundingMode.HALF_UP);

        if (product.getSellingPrice() == null || product.getSellingPrice().compareTo(calculatedSellingPrice) < 0) {

            product.setSellingPrice(calculatedSellingPrice);
        }

        productRepository.save(product);
    }


    private void validateVendorChange(Purchase purchase, Long newVendorId) {

        Long currentVendorId = purchase.getVendor().getId();

        if (currentVendorId.equals(newVendorId)) {
            return;
        }

        if (paymentRepository.existsByPurchaseId(purchase.getId())) {
            throw new RestApiException(ErrorDescription.PURCHASE_VENDOR_CHANGE_HAS_PAYMENTS, HttpStatus.CONFLICT);
        }
    }


    private void validatePurchaseItems(NewPurchaseDto dto) {
        if (dto.getPurchaseItems() == null || dto.getPurchaseItems().isEmpty()) {
            throw new RestApiException(ErrorDescription.NO_PRODUCT_IN_PURCHASE);
        }
    }

    private Customer getVendorOrThrow(Long vendorId) {
        return customerRepository.findById(vendorId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.CUSTOMER_NOT_FOUND.getDescription()));
    }

    private TypeOfDocument getDocumentOrThrow(Long documentId) {
        return typeOfDocumentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.TYPE_OF_DOCUMENT_NOT_FOUND.getDescription()));
    }

    private Purchase getPurchaseOrThrow(Long purchaseId) {
        return purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.PURCHASE_NOT_FOUND.getDescription()));
    }

    private TypeOfOperation parseTypeOfOperation(String type) {
        if (type == null) {
            throw new RestApiException(ErrorDescription.INVALID_OPERATION_TYPE, HttpStatus.BAD_REQUEST);
        }
        try {
            return TypeOfOperation.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RestApiException(ErrorDescription.INVALID_OPERATION_TYPE, HttpStatus.BAD_REQUEST);
        }
    }


    private PurchaseDto toPurchaseDto(Purchase purchase) {
        PurchaseDto dto = modelMapper.map(purchase, PurchaseDto.class);
        dto.setHasPayments(paymentRepository.existsByPurchaseId(purchase.getId()));
        return dto;
    }


}
