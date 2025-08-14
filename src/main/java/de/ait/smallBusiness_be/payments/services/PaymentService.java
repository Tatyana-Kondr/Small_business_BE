package de.ait.smallBusiness_be.payments.services;

import de.ait.smallBusiness_be.payments.dto.NewPaymentDto;
import de.ait.smallBusiness_be.payments.dto.PaymentDto;
import de.ait.smallBusiness_be.payments.dto.PaymentPrefillDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


public interface PaymentService {
    PaymentPrefillDto getPrefillDataForSale(Long saleId);
    PaymentPrefillDto getPrefillDataForPurchase(Long purchaseId);
    PaymentDto createPayment(NewPaymentDto newPaymentDto);
    Page<PaymentDto> getPayments(Pageable pageable);
    Page<PaymentDto> searchPayments(Pageable pageable, String query);
    Page<PaymentDto> getAllPaymentsByFilter(Pageable pageable,
                                            Long id,
                                            Long customerId,
                                            String customerName,
                                            Long saleId,
                                            Long purchaseId,
                                            LocalDate startDate,
                                            LocalDate endDate,
                                            String document,
                                            String documentNumber,
                                            BigDecimal amount,
                                            String searchQuery);
    PaymentDto getPayment(Long id);
    PaymentDto updatePayment(Long id, NewPaymentDto newPaymentDto);
    void deletePayment(Long id);
    List<Long> getAllSaleIds();
    List<Long> getAllPurchaseIds();
}
