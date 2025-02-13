package de.ait.smallBusiness_be.purchases.services;

import de.ait.smallBusiness_be.customers.dao.CustomerRepository;
import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.products.dao.ProductRepository;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.purchases.dao.PurchaseItemRepository;
import de.ait.smallBusiness_be.purchases.dao.PurchaseRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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
    private final PurchaseItemRepository purchaseItemRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public PurchaseDto createPurchase(NewPurchaseDto newPurchaseDto) {
        Customer customer = customerRepository.findById(newPurchaseDto.getVendorId())
                .orElseThrow(() -> new EntityNotFoundException("Vendor not found"));

        Purchase purchase = modelMapper.map(newPurchaseDto, Purchase.class);
        purchase.setVendor(customer);

        AtomicReference<BigDecimal> subtotal = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> taxSum = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> total = new AtomicReference<>(BigDecimal.ZERO);

        // Проверяем и добавляем PurchaseItem к Purchase
        if (newPurchaseDto.getPurchaseItems() != null && !newPurchaseDto.getPurchaseItems().isEmpty()) {
            List<PurchaseItem> purchaseItems = newPurchaseDto.getPurchaseItems().stream()
                    .map(newPurchaseItemDto -> {
                        // Находим продукт по ID
                        Product product = productRepository.findById(newPurchaseItemDto.getProduct().getId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                        "Product not found with ID: " + newPurchaseItemDto.getProduct().getId()
                                ));

                        //Найти максимальное значение position среди уже существующих PurchaseItem для данного purchase_id.
                        //Назначить position = maxPosition + 1 (если maxPosition отсутствует, то position = 1).
                        Integer maxPosition = purchaseItemRepository.findMaxPositionByPurchaseId(purchase.getId());
                        int newPosition = (maxPosition != null) ? maxPosition + 1 : 1;

                        // Создаем PurchaseItem и устанавливаем продукт
                        PurchaseItem purchaseItem = modelMapper.map(newPurchaseItemDto, PurchaseItem.class);
                        purchaseItem.setProduct(product);
                        // Устанавливаем связь с Purchase
                        purchaseItem.setPurchase(purchase);
                        //Устанавливаем позицию
                        purchaseItem.setPosition(newPosition);

                        // Рассчитываем totalPrice, taxAmount и totalAmount
                        BigDecimal totalPrice = purchaseItem
                                .getUnitPrice().multiply(BigDecimal.valueOf(purchaseItem.getQuantity()));
                        BigDecimal taxAmount = totalPrice
                                .multiply(purchaseItem.getTaxPercentage().movePointLeft(2)) // Это эквивалентно делению на 100.
                                .setScale(2, RoundingMode.HALF_UP);
                        BigDecimal totalAmount = totalPrice.add(taxAmount);

                        purchaseItem.setTotalPrice(totalPrice);
                        purchaseItem.setTaxAmount(taxAmount);
                        purchaseItem.setTotalAmount(totalAmount);

                        // Обновляем суммы через AtomicReference
                        subtotal.updateAndGet(value -> value.add(totalPrice));
                        taxSum.updateAndGet(value -> value.add(taxAmount));
                        total.updateAndGet(value -> value.add(totalAmount));

                        return purchaseItem;
                    })
                    .collect(Collectors.toList());

            // Устанавливаем PurchaseItems в Purchase
            purchase.setPurchaseItems(purchaseItems);
        }

        // Устанавливаем пересчитанные суммы в Purchase
        purchase.setSubtotal(subtotal.get());
        purchase.setTaxSum(taxSum.get());
        purchase.setTotal(total.get());

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
                // Если поле неверное, заменяем сортировку по умолчанию
                pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "purchasingDate"));
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
    public Page<PurchaseDto> getAllPurchasesByFilter(Pageable pageable, Long id, Long vendorId, String document, String documentNumber, BigDecimal total, String paymentStatus) {
        return purchaseRepository.filterByFields(pageable, id, vendorId, document, documentNumber, total, paymentStatus)
                .map(purchase -> modelMapper.map(purchase, PurchaseDto.class));
    }

    @Override
    @Transactional
    public PurchaseDto updatePurchase(Long id, NewPurchaseDto newPurchaseDto) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Purchase not found"));

        Customer customer = customerRepository.findById(newPurchaseDto.getVendorId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        purchase.setVendor(customer);
        purchase.setPurchasingDate(newPurchaseDto.getPurchasingDate());

        try {
            purchase.setDocument(TypeOfDocument.valueOf(newPurchaseDto.getDocument().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid type of document: " + newPurchaseDto.getDocument());
        }

        purchase.setDocumentNumber(newPurchaseDto.getDocumentNumber());

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

        // Загружаем текущие элементы
        Map<Long, PurchaseItem> existingItems = purchase.getPurchaseItems().stream()
                .collect(Collectors.toMap(item -> item.getProduct().getId(), item -> item));

        List<Long> newProductIds = newPurchaseDto.getPurchaseItems().stream()
                .map(item -> item.getProduct().getId())
                .collect(Collectors.toList());

        // Пересчет итоговых сумм
        AtomicReference<BigDecimal> subtotal = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> taxSum = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> total = new AtomicReference<>(BigDecimal.ZERO);

        List<PurchaseItem> updatedItems = new ArrayList<>();

        for (NewPurchaseItemDto itemDto : newPurchaseDto.getPurchaseItems()) {
            Long productId = itemDto.getProduct().getId();
            PurchaseItem purchaseItem = existingItems.get(productId);

            if (purchaseItem == null) {
                purchaseItem = new PurchaseItem();
                purchaseItem.setPurchase(purchase);
                purchaseItem.setProduct(productRepository.findById(productId)
                        .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId)));
                purchaseItem.setPosition(updatedItems.size() + 1);
            }

            purchaseItem.setQuantity(itemDto.getQuantity());
            purchaseItem.setUnitPrice(itemDto.getUnitPrice());

            BigDecimal totalPrice = purchaseItem.getUnitPrice().multiply(BigDecimal.valueOf(purchaseItem.getQuantity()));
            BigDecimal taxAmount = totalPrice.multiply(purchaseItem.getTaxPercentage().movePointLeft(2))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalAmount = totalPrice.add(taxAmount);

            purchaseItem.setTotalPrice(totalPrice);
            purchaseItem.setTaxAmount(taxAmount);
            purchaseItem.setTotalAmount(totalAmount);

            subtotal.updateAndGet(value -> value.add(totalPrice));
            taxSum.updateAndGet(value -> value.add(taxAmount));
            total.updateAndGet(value -> value.add(totalAmount));

            updatedItems.add(purchaseItem);
        }

        // Удаляем старые элементы, которых нет в новом списке (НЕ заменяем коллекцию)
        purchase.getPurchaseItems().removeIf(item -> !newProductIds.contains(item.getProduct().getId()));

        // Добавляем/обновляем существующие элементы
        for (PurchaseItem updatedItem : updatedItems) {
            if (!purchase.getPurchaseItems().contains(updatedItem)) {
                purchase.getPurchaseItems().add(updatedItem);
            }
        }

        purchase.setSubtotal(subtotal.get());
        purchase.setTaxSum(taxSum.get());
        purchase.setTotal(total.get());

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

}
