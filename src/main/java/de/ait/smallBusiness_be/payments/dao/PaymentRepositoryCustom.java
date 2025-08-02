package de.ait.smallBusiness_be.payments.dao;

import de.ait.smallBusiness_be.payments.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PaymentRepositoryCustom {
    Page<Payment> searchPayments(Pageable pageable, String searchQuery);
    Page<Payment> filterByPaymentsFields(Pageable pageable,
                                         Long id,
                                         Long customerId,
                                         String customerName,
                                         LocalDate startDate,
                                         LocalDate endDate,
                                         String document,
                                         String documentNumber,
                                         BigDecimal amount,
                                         String searchQuery);
}
