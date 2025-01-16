package de.ait.smallBusiness_be.purchases.services;

import de.ait.smallBusiness_be.customers.dao.CustomerRepository;
import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.purchases.dao.PurchaseRepository;
import de.ait.smallBusiness_be.purchases.dto.NewPurchaseDto;
import de.ait.smallBusiness_be.purchases.dto.PurchaseDto;
import de.ait.smallBusiness_be.purchases.model.PaymentStatus;
import de.ait.smallBusiness_be.purchases.model.Purchase;
import de.ait.smallBusiness_be.purchases.model.TypeOfDocument;
import de.ait.smallBusiness_be.purchases.model.TypeOfOperation;
import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

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
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public PurchaseDto createPurchase(NewPurchaseDto newPurchaseDto) {
        Customer customer = customerRepository.findById(newPurchaseDto.getVendorId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
        Purchase purchase = modelMapper.map(newPurchaseDto, Purchase.class);
        purchase.setVendor(customer);
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
