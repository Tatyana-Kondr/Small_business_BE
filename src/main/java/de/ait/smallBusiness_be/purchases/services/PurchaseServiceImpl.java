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
import de.ait.smallBusiness_be.purchases.dto.PurchaseDto;
import de.ait.smallBusiness_be.purchases.model.*;
import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
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

        // Проверяем и добавляем PurchaseItem к Purchase
        if (newPurchaseDto.getPurchaseItems() != null && !newPurchaseDto.getPurchaseItems().isEmpty()) {
            List<PurchaseItem> purchaseItems = newPurchaseDto.getPurchaseItems().stream()
                    .map(newPurchaseItemDto -> {
                        // Находим продукт по ID
                        Product product = productRepository.findById(newPurchaseItemDto.getProduct().getId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                        "Product not found with ID: " + newPurchaseItemDto.getProduct().getId()
                                ));

                        // Создаем PurchaseItem и устанавливаем продукт
                        PurchaseItem purchaseItem = modelMapper.map(newPurchaseItemDto, PurchaseItem.class);
                        purchaseItem.setProduct(product);

                        // Устанавливаем связь с Purchase
                        purchaseItem.setPurchase(purchase);

                        return purchaseItem;
                    })
                    .collect(Collectors.toList());

            // Устанавливаем PurchaseItems в Purchase
            purchase.setPurchaseItems(purchaseItems);
        }

        Purchase savedPurchase = purchaseRepository.save(purchase);

        return modelMapper.map(savedPurchase, PurchaseDto.class);
    }

    @Override
    public Page<PurchaseDto> getAllPurchases(Pageable pageable) {
        Page<Purchase> purchases = purchaseRepository.findAll(pageable);
        if (purchases.isEmpty()) {
            throw new RestApiException(ErrorDescription.LIST_IS_EMPTY, HttpStatus.NOT_FOUND);
        }
        return purchases.map(purchase -> modelMapper.map(purchase, PurchaseDto.class));
    }

    @Override
    public PurchaseDto getPurchaseById(Long id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Purchase not found"));
        return modelMapper.map(purchase, PurchaseDto.class);
    }

    @Override
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
        // Преобразуем строку Document
        try {
            TypeOfDocument document = TypeOfDocument.valueOf(newPurchaseDto.getDocument().toUpperCase());
            purchase.setDocument(document);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid type ofdocument: " + newPurchaseDto.getDocument());
        }
        purchase.setDocumentNumber(newPurchaseDto.getDocumentNumber());
        // Преобразуем строку Type
        try{
            TypeOfOperation type = TypeOfOperation.valueOf(newPurchaseDto.getType().toUpperCase());
            purchase.setType(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid type of operation: " + newPurchaseDto.getType());
        }
        purchase.setTax(newPurchaseDto.getTax());
        purchase.setSubtotal(newPurchaseDto.getSubtotal());
        purchase.setTaxSum(newPurchaseDto.getTaxSum());
        purchase.setTotal(newPurchaseDto.getTotal());
        // Преобразуем строку PaymentStatus
        try {
            PaymentStatus status = PaymentStatus.valueOf(newPurchaseDto.getPaymentStatus().toUpperCase());
            purchase.setPaymentStatus(status);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid payment status: " + newPurchaseDto.getPaymentStatus());
        }

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
