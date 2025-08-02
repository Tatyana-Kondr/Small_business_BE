package de.ait.smallBusiness_be.payments.services;

import de.ait.smallBusiness_be.payments.dto.NewPaymentProcessDto;
import de.ait.smallBusiness_be.payments.dto.PaymentProcessDto;
import java.util.List;

public interface PaymentProcessService {
    PaymentProcessDto createPaymentProcess(NewPaymentProcessDto newPaymentProcessDto);
    List<PaymentProcessDto> getAllPaymentProcesses();
    PaymentProcessDto getPaymentProcessById(Long id);
    PaymentProcessDto updatePaymentProcess(Long id, NewPaymentProcessDto newPaymentProcessDto);
    void deletePaymentProcess(Long id);
}
