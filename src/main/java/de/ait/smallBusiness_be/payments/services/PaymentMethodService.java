package de.ait.smallBusiness_be.payments.services;

import de.ait.smallBusiness_be.payments.dto.NewPaymentMethodDto;
import de.ait.smallBusiness_be.payments.dto.PaymentMethodDto;

import java.util.List;

public interface PaymentMethodService {
    PaymentMethodDto createPaymentMethod(NewPaymentMethodDto newPaymentMethodDto);
    List<PaymentMethodDto> getAllPaymentMethods();
    PaymentMethodDto getPaymentMethodById(Long id);
    PaymentMethodDto updatePaymentMethod(Long id, NewPaymentMethodDto newPaymentMethodDto);
    void deletePaymentMethod(Long id);
}
