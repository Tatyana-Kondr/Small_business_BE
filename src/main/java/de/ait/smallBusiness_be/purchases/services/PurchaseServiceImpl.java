package de.ait.smallBusiness_be.purchases.services;

import de.ait.smallBusiness_be.customers.dao.CustomerRepository;
import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.payments.dao.PaymentRepository;
import de.ait.smallBusiness_be.payments.model.Payment;
import de.ait.smallBusiness_be.products.dao.ProductRepository;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.purchases.dao.PurchaseRepository;
import de.ait.smallBusiness_be.purchases.dao.TypeOfDocumentRepository;
import de.ait.smallBusiness_be.purchases.dto.NewPurchaseDto;
import de.ait.smallBusiness_be.purchases.dto.NewPurchaseItemDto;
import de.ait.smallBusiness_be.purchases.dto.PurchaseDto;
import de.ait.smallBusiness_be.purchases.model.*;
import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


/**
 * 16.01.2025
 * SmB
 *
 * @author Kondratyeva (AIT TR)
 */

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService{

    private final PurchaseRepository purchaseRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final TypeOfDocumentRepository typeOfDocumentRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public PurchaseDto createPurchase(NewPurchaseDto newPurchaseDto) {
        Customer customer = customerRepository.findById(newPurchaseDto.getVendorId())
                .orElseThrow(() -> new EntityNotFoundException("Vendor not found"));

        TypeOfDocument document = typeOfDocumentRepository.findById(newPurchaseDto.getDocumentId())
                .orElseThrow(() -> new EntityNotFoundException("Type of document not found."));

        if (newPurchaseDto.getPurchaseItems() == null || newPurchaseDto.getPurchaseItems().isEmpty()) {
            throw new RestApiException(ErrorDescription.NO_PRODUCT_IN_PURCHASE);
        }

        Purchase purchase = modelMapper.map(newPurchaseDto, Purchase.class);
        purchase.setVendor(customer);
        purchase.setDocument(document);

        calculatePurchaseAmounts(purchase, newPurchaseDto);

        Purchase savedPurchase = purchaseRepository.save(purchase);

        return modelMapper.map(savedPurchase, PurchaseDto.class);
    }

    @Override
    @Transactional
    public Page<PurchaseDto> getAllPurchases(Pageable pageable) {
        // Проверяем, корректно ли передана сортировка
        List<String> allowedSortFields = List.of("purchasingDate", "docNr", "amount"); // допустимые поля
        Sort sort = pageable.getSort();

        for (Sort.Order order : sort) {
            if (!allowedSortFields.contains(order.getProperty())) {
                pageable = PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(
                                Sort.Order.desc("purchasingDate"),
                                Sort.Order.desc("documentNumber")
                        )
                );
                break;
            }
        }

        Page<Purchase> purchases = purchaseRepository.findAll(pageable);

        if (purchases.isEmpty()) {
            throw new RestApiException(ErrorDescription.LIST_IS_EMPTY, HttpStatus.NOT_FOUND);
        }

        return purchases.map(purchase -> modelMapper.map(purchase, PurchaseDto.class));
    }

    @Override
    @Transactional
    public PurchaseDto getPurchaseById(Long id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Purchase not found"));
        return modelMapper.map(purchase, PurchaseDto.class);
    }

    @Override
    @Transactional
    public Page<PurchaseDto> searchPurchases(Pageable pageable, String query) {
        return purchaseRepository.searchPurchases(pageable, query)
                .map(purchase -> modelMapper.map(purchase, PurchaseDto.class));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseDto> getAllPurchasesByFilter(Pageable pageable, Long id, Long vendorId, String vendorName, Long documentId, String documentNumber, BigDecimal total, String paymentStatus, LocalDate startDate, LocalDate endDate, String searchQuery) {
        return purchaseRepository.filterByFields(pageable, id, vendorId, vendorName, documentId, documentNumber, total, paymentStatus, startDate, endDate, searchQuery)
                .map(purchase -> modelMapper.map(purchase, PurchaseDto.class));
    }

    @Override
    @Transactional
    public PurchaseDto updatePurchase(Long id, NewPurchaseDto newPurchaseDto) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Purchase not found"));

        Customer customer = customerRepository.findById(newPurchaseDto.getVendorId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        TypeOfDocument document = typeOfDocumentRepository.findById(newPurchaseDto.getDocumentId())
                .orElseThrow(() -> new EntityNotFoundException("Type of document not found."));

        // Обновляем базовые поля
        purchase.setVendor(customer);
        purchase.setDocument(document);
        purchase.setPurchasingDate(newPurchaseDto.getPurchasingDate());

        try {
            purchase.setType(TypeOfOperation.valueOf(newPurchaseDto.getType().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid type of operation: " + newPurchaseDto.getType());
        }

        try {
            purchase.setPaymentStatus(PaymentStatus.valueOf(newPurchaseDto.getPaymentStatus().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid payment status: " + newPurchaseDto.getPaymentStatus());
        }

        purchase.setDocumentNumber(newPurchaseDto.getDocumentNumber());

        // Удаляем все старые позиции
        purchase.getPurchaseItems().clear();

        calculatePurchaseAmounts(purchase, newPurchaseDto);
        // Сохраняем
        Purchase updatedPurchase = purchaseRepository.save(purchase);
        return modelMapper.map(updatedPurchase, PurchaseDto.class);
    }


    @Override
    public void deletePurchase(Long id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Purchase not found"));
        purchaseRepository.delete(purchase);
    }

    @Override
    @Transactional
    public PurchaseDto updatePaymentStatus(Long purchaseId) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new EntityNotFoundException("Purchase not found"));

        // Получаем список оплат по purchaseId
        List<Payment> payments = paymentRepository.findByPurchaseId(purchaseId);

        BigDecimal totalPaid = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PaymentStatus newStatus;

        if (totalPaid.compareTo(BigDecimal.ZERO) == 0) {
            newStatus = PaymentStatus.AUSSTEHEND;
        } else if (totalPaid.compareTo(purchase.getTotal()) >= 0) {
            newStatus = PaymentStatus.BEZAHLT;
        } else {
            newStatus = PaymentStatus.ANZAHLUNG;
        }

        // Обновляем статус, если он изменился
        if (purchase.getPaymentStatus() != newStatus) {
            purchase.setPaymentStatus(newStatus);
            purchaseRepository.save(purchase);
        }
        return modelMapper.map(purchase, PurchaseDto.class);
    }

    private void calculatePurchaseAmounts(Purchase purchase, NewPurchaseDto newPurchaseDto) {
        AtomicReference<BigDecimal> subtotal = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> taxSum = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> total = new AtomicReference<>(BigDecimal.ZERO);

        List<PurchaseItem> purchaseItems = newPurchaseDto.getPurchaseItems().stream()
                .map(dto -> createPurchaseItem(purchase, dto, subtotal, taxSum, total, newPurchaseDto.getPurchasingDate()))
                .toList();

        purchase.setPurchaseItems(purchaseItems);
        purchase.setSubtotal(subtotal.get());
        purchase.setTaxSum(taxSum.get());
        purchase.setTotal(total.get());
    }

    private PurchaseItem createPurchaseItem(
            Purchase purchase,
            NewPurchaseItemDto dto,
            AtomicReference<BigDecimal> subtotal,
            AtomicReference<BigDecimal> taxSum,
            AtomicReference<BigDecimal> total,
            LocalDate purchasingDate) {

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + dto.getProductId()));

        PurchaseItem item = modelMapper.map(dto, PurchaseItem.class);
        item.setPurchase(purchase);
        item.setProduct(product);

        BigDecimal totalPrice = dto.getUnitPrice().multiply(dto.getQuantity());
        BigDecimal taxAmount = totalPrice.multiply(dto.getTaxPercentage().movePointLeft(2))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = totalPrice.add(taxAmount);

        item.setTotalPrice(totalPrice);
        item.setTaxAmount(taxAmount);
        item.setTotalAmount(totalAmount);

        subtotal.updateAndGet(v -> v.add(totalPrice));
        taxSum.updateAndGet(v -> v.add(taxAmount));
        total.updateAndGet(v -> v.add(totalAmount));

        updateProductAfterPurchase(product, dto.getUnitPrice(), purchasingDate);

        return item;
    }

    private void updateProductAfterPurchase(Product product, BigDecimal unitPrice, LocalDate purchasingDate) {
        boolean updated = false;

        // Обновление закупочной цены
        if (product.getPurchasingPrice() == null || unitPrice.compareTo(product.getPurchasingPrice()) > 0) {
            product.setPurchasingPrice(unitPrice);
            updated = true;

            // Пересчёт продажной цены, если нужно
            BigDecimal calculatedSellingPrice = unitPrice
                    .multiply(BigDecimal.valueOf(1.2))
                    .setScale(2, RoundingMode.HALF_UP);

            if (product.getSellingPrice() == null || product.getSellingPrice().compareTo(calculatedSellingPrice) < 0) {
                product.setSellingPrice(calculatedSellingPrice);
            }
        }

        // Обновление даты последней закупки
        LocalDateTime newPurchaseDateTime = purchasingDate.atStartOfDay();
        if (product.getDateOfLastPurchase() == null || newPurchaseDateTime.isAfter(product.getDateOfLastPurchase())) {
            product.setDateOfLastPurchase(newPurchaseDateTime);
            updated = true;
        }

        if (updated) {
            productRepository.save(product);
        }
    }

}
