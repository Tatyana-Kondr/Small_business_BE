package de.ait.smallBusiness_be.sales.controllers;

import de.ait.smallBusiness_be.sales.controllers.api.SalesApi;
import de.ait.smallBusiness_be.sales.dto.NewSaleDto;
import de.ait.smallBusiness_be.sales.dto.SaleDto;
import de.ait.smallBusiness_be.sales.services.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 18.02.2025
 * SmB_be
 *
 * @author Kondratyeva (AIT TR)
 */

@RestController
@RequiredArgsConstructor
public class SaleController implements SalesApi {

    private  final SaleService saleService;

    @Override
    public SaleDto addSale(NewSaleDto newSaleDto) {
        return saleService.createSale(newSaleDto);
    }

    @Override
    public Page<SaleDto> getAllSales(Pageable pageable) {
        return saleService.getAllSales(pageable);
    }

    @Override
    public SaleDto getSaleById(Long id) {
        return saleService.getSaleById(id);
    }

    @Override
    public Page<SaleDto> searchSales(Pageable pageable, String sort, String query) {
        return saleService.searchSales(pageable, query);
    }

    @Override
    public Page<SaleDto> getAllSalesByFilter(Pageable pageable, String sort, Long id, Long customerId, String customerName, String invoiceNumber, BigDecimal totalAmount, String paymentStatus, LocalDate startDate, LocalDate endDate, String searchQuery) {
        return saleService.getAllSalesByFilter(pageable, id, customerId, customerName, invoiceNumber, totalAmount, paymentStatus, startDate, endDate, searchQuery);
    }

    @Override
    public SaleDto updateSale(Long id, NewSaleDto newSaleDto) {
        return saleService.updateSale(id, newSaleDto);
    }

    @Override
    public void deleteSale(Long id) {
    saleService.deleteSale(id);
    }
}
