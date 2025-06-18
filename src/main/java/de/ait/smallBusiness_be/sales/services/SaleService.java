package de.ait.smallBusiness_be.sales.services;

import de.ait.smallBusiness_be.purchases.dto.PurchaseDto;
import de.ait.smallBusiness_be.sales.dto.NewSaleDto;
import de.ait.smallBusiness_be.sales.dto.SaleDto;
import de.ait.smallBusiness_be.sales.models.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface SaleService {

    SaleDto createSale(NewSaleDto newSale);
    Page<SaleDto> getAllSales(Pageable pageable);
    SaleDto getSaleById(Long id);
    SaleDto updateSale(Long saleId, NewSaleDto updateSale);
    void deleteSale(Long saleId);
    Sale getSaleOrThrow(Long id);
    boolean checkIfSaleExistsById(Long saleId);
    Page<SaleDto> searchSales(Pageable pageable, String query);
    Page<SaleDto> getAllSalesByFilter(Pageable pageable,
                                      Long id,
                                      Long customerId,
                                      String customerName,
                                      String invoiceNumber,
                                      BigDecimal totalAmount,
                                      String paymentStatus,
                                      LocalDate startDate,
                                      LocalDate endDate,
                                      String searchQuery);
}
