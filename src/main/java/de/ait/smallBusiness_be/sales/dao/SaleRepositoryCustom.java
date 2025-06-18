package de.ait.smallBusiness_be.sales.dao;

import de.ait.smallBusiness_be.sales.models.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface SaleRepositoryCustom {
    Page<Sale> searchSales(Pageable pageable, String searchQuery);

    Page<Sale> filterSalesByFields(Pageable pageable, Long id, Long customerId, String customerName, String invoiceNumber, BigDecimal totalAmount, String paymentStatus, LocalDate startDate,
                                  LocalDate endDate, String searchQuery);
}
