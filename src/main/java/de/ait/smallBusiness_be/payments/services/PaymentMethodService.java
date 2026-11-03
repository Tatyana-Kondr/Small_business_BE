package de.ait.smallBusiness_be.payments.services;

import de.ait.smallBusiness_be.payments.dto.NewPaymentMethodDto;
import de.ait.smallBusiness_be.payments.dto.PaymentMethodDto;
import de.ait.smallBusiness_be.payments.model.PaymentMethod;

import java.util.List;

public interface PaymentMethodService {
    PaymentMethodDto createPaymentMethod(NewPaymentMethodDto newPaymentMethodDto);
    List<PaymentMethodDto> getAllPaymentMethods();
    List<PaymentMethodDto> getActivePaymentMethods();
    PaymentMethodDto getPaymentMethodById(Long id);
    PaymentMethodDto updatePaymentMethod(Long id, NewPaymentMethodDto newPaymentMethodDto);
    void deletePaymentMethod(Long id);
    PaymentMethod getPaymentMethodOrThrow(Long id);
}
