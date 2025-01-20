package de.ait.smallBusiness_be.purchases.controllers;

import de.ait.smallBusiness_be.purchases.controllers.api.PurchasesApi;
import de.ait.smallBusiness_be.purchases.dto.NewPurchaseDto;
import de.ait.smallBusiness_be.purchases.dto.PurchaseDto;
import de.ait.smallBusiness_be.purchases.services.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * 17.01.2025
 * SmB
 *
 * @author Kondratyeva (AIT TR)
 */

@RestController
@RequiredArgsConstructor
public class PurchaseController implements PurchasesApi{

    private final PurchaseService purchaseService;

    @Override
    public PurchaseDto createPurchase(NewPurchaseDto newPurchaseDto) {
        return purchaseService.createPurchase(newPurchaseDto);
    }

    @Override
    public Page<PurchaseDto> getAllPurchases(Pageable pageable, String sort) {
        return purchaseService.getAllPurchases(pageable);
    }

    @Override
    public PurchaseDto getPurchaseById(Long id) {
        return purchaseService.getPurchaseById(id);
    }

    @Override
    public Page<PurchaseDto> searchPurchases(Pageable pageable, String sort, String query) {
        return purchaseService.searchPurchases(pageable, query);
    }

    @Override
    public Page<PurchaseDto> getAllPurchasesByFilter(Pageable pageable, String sort, Long id, Long vendorId, String document, String documentNumber, BigDecimal total, String paymentStatus) {
        return purchaseService.getAllPurchasesByFilter(pageable, id, vendorId, document, documentNumber, total, paymentStatus);
    }

    @Override
    public PurchaseDto updatePurchase(Long id, NewPurchaseDto newPurchaseDto) {
        return purchaseService.updatePurchase(id, newPurchaseDto);
    }

    @Override
    public void deletePurchase(Long id) {
        purchaseService.deletePurchase(id);
    }
}
