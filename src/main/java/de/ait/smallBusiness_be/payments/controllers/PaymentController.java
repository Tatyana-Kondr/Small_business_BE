package de.ait.smallBusiness_be.payments.controllers;

import de.ait.smallBusiness_be.payments.controllers.api.PaymentApi;
import de.ait.smallBusiness_be.payments.dto.NewPaymentDto;
import de.ait.smallBusiness_be.payments.dto.PaymentDto;
import de.ait.smallBusiness_be.payments.dto.PaymentPrefillDto;
import de.ait.smallBusiness_be.payments.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {

    private final PaymentService paymentService;

    @Override
    public PaymentDto addPayment(NewPaymentDto newPaymentDto) {
        return paymentService.createPayment(newPaymentDto);
    }

    @Override
    public Page<PaymentDto> getAllPayments(Pageable pageable) {
        return paymentService.getPayments(pageable);
    }

    @Override
    public Page<PaymentDto> searchPayments(Pageable pageable, String sort, String query) {
        return paymentService.searchPayments(pageable, query);
    }

    @Override
    public Page<PaymentDto> getAllPaymentsByFilter(Pageable pageable, String sort, Long id, Long customerId, String customerName, LocalDate startDate, LocalDate endDate, String document, String documentNumber, BigDecimal amount, String searchQuery) {
        return paymentService.getAllPaymentsByFilter(pageable, id, customerId, customerName, startDate, endDate, document, documentNumber, amount, searchQuery);
    }

    @Override
    public PaymentDto getPaymentById(Long id) {
        return paymentService.getPayment(id);
    }

    @Override
    public PaymentDto updatePayment(Long id, NewPaymentDto newPaymentDto) {
        return paymentService.updatePayment(id, newPaymentDto);
    }

    @Override
    public void deletePayment(Long id) {
        paymentService.deletePayment(id);
    }

    @Override
    public PaymentPrefillDto getPrefillDataForSale(Long saleId) {
        return paymentService.getPrefillDataForSale(saleId);
    }

    @Override
    public PaymentPrefillDto getPrefillDataForPurchase(Long purchaseId) {
        return paymentService.getPrefillDataForPurchase(purchaseId);
    }

}
