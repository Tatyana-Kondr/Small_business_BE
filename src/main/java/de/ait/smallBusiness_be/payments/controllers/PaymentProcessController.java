package de.ait.smallBusiness_be.payments.controllers;

import de.ait.smallBusiness_be.payments.controllers.api.PaymentProcessApi;
import de.ait.smallBusiness_be.payments.dto.NewPaymentProcessDto;
import de.ait.smallBusiness_be.payments.dto.PaymentProcessDto;
import de.ait.smallBusiness_be.payments.services.PaymentProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PaymentProcessController implements PaymentProcessApi {

    private final PaymentProcessService paymentProcessService;

    @Override
    public PaymentProcessDto addPaymentProcess(NewPaymentProcessDto newPaymentProcessDto) {
        return paymentProcessService.createPaymentProcess(newPaymentProcessDto);
    }

    @Override
    public List<PaymentProcessDto> getAllPaymentProcesses() {
        return paymentProcessService.getAllPaymentProcesses();
    }

    @Override
    public PaymentProcessDto getPaymentProcessById(Long id) {
        return paymentProcessService.getPaymentProcessById(id);
    }

    @Override
    public PaymentProcessDto updatePaymentProcess(Long id, NewPaymentProcessDto newPaymentProcessDto) {
        return paymentProcessService.updatePaymentProcess(id, newPaymentProcessDto);
    }

    @Override
    public void deletePaymentProcess(Long id) {
        paymentProcessService.deletePaymentProcess(id);
    }
}
