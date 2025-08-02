package de.ait.smallBusiness_be.payments.controllers;

import de.ait.smallBusiness_be.payments.controllers.api.PaymentMethodApi;
import de.ait.smallBusiness_be.payments.dto.NewPaymentMethodDto;
import de.ait.smallBusiness_be.payments.dto.PaymentMethodDto;
import de.ait.smallBusiness_be.payments.services.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PaymentMethodController implements PaymentMethodApi {

    private final PaymentMethodService paymentMethodService;

    @Override
    public PaymentMethodDto addPaymentMethod(NewPaymentMethodDto newPaymentMethodDto) {
        return paymentMethodService.createPaymentMethod(newPaymentMethodDto);
    }

    @Override
    public List<PaymentMethodDto> getAllPaymentMethods() {
        return paymentMethodService.getAllPaymentMethods();
    }

    @Override
    public PaymentMethodDto getPaymentMethodById(Long id) {
        return paymentMethodService.getPaymentMethodById(id);
    }

    @Override
    public PaymentMethodDto updatePaymentMethod(Long id, NewPaymentMethodDto newPaymentMethodDto) {
        return paymentMethodService.updatePaymentMethod(id, newPaymentMethodDto);
    }

    @Override
    public void deletePaymentMethod(Long id) {
        paymentMethodService.deletePaymentMethod(id);
    }
}
