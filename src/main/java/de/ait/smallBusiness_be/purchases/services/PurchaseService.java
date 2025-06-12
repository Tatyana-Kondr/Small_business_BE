package de.ait.smallBusiness_be.purchases.services;

import de.ait.smallBusiness_be.purchases.dto.NewPurchaseDto;
import de.ait.smallBusiness_be.purchases.dto.PurchaseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PurchaseService {

    PurchaseDto createPurchase(NewPurchaseDto newPurchaseDto);
    Page<PurchaseDto> getAllPurchases(Pageable pageable);
    PurchaseDto getPurchaseById(Long id);
    Page<PurchaseDto> searchPurchases(Pageable pageable, String query);
    Page<PurchaseDto> getAllPurchasesByFilter(Pageable pageable, Long id, Long vendorId, String vendorName, String document, String documentNumber,
                                              BigDecimal total, String paymentStatus, LocalDate startDate, LocalDate endDate, String searchQuery);
    PurchaseDto updatePurchase(Long id, NewPurchaseDto newPurchaseDto);
    void deletePurchase(Long id);
}
