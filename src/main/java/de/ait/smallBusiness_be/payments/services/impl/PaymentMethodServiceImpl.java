package de.ait.smallBusiness_be.payments.services.impl;

import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.payments.dao.PaymentMethodRepository;
import de.ait.smallBusiness_be.payments.dto.NewPaymentMethodDto;
import de.ait.smallBusiness_be.payments.dto.PaymentMethodDto;
import de.ait.smallBusiness_be.payments.model.PaymentMethod;
import de.ait.smallBusiness_be.payments.services.PaymentMethodService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final ModelMapper modelMapper;

    @Override
    public PaymentMethodDto createPaymentMethod(NewPaymentMethodDto newPaymentMethodDto) {
        boolean exists = paymentMethodRepository
                .existsPaymentMethodByProviderAndMaskedNumber(
                        newPaymentMethodDto.getProvider(),
                        newPaymentMethodDto.getMaskedNumber()
                );

        if (exists) {
            throw new IllegalArgumentException("A payment method with this provider and masked number already exists.");
        }

        PaymentMethod method = PaymentMethod.builder()
                .provider(newPaymentMethodDto.getProvider())
                .maskedNumber(newPaymentMethodDto.getMaskedNumber())
                .details(newPaymentMethodDto.getDetails())
                .active(newPaymentMethodDto.isActive())
                .build();

        return modelMapper.map(paymentMethodRepository.save(method), PaymentMethodDto.class);
    }

    @Override
    public List<PaymentMethodDto> getAllPaymentMethods() {
        List<PaymentMethod> paymentMethods = paymentMethodRepository.findAll();
        if (paymentMethods.isEmpty()) {throw new RestApiException(ErrorDescription.LIST_IS_EMPTY, HttpStatus.NOT_FOUND);}
        return paymentMethods.stream().map(paymentMethod -> modelMapper.map(paymentMethod, PaymentMethodDto.class)).collect(Collectors.toList());
    }

    @Override
    public PaymentMethodDto getPaymentMethodById(Long id) {
        PaymentMethod method = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment method with ID " + id + " not found"));
        return modelMapper.map(method, PaymentMethodDto.class);
    }

    @Override
    public PaymentMethodDto updatePaymentMethod(Long id, NewPaymentMethodDto newPaymentMethodDto) {
        PaymentMethod existing = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment method with ID " + id + " not found"));

        if (!existing.getProvider().equals(newPaymentMethodDto.getProvider()) ||
                !Objects.equals(existing.getMaskedNumber(), newPaymentMethodDto.getMaskedNumber())) {

            boolean exists = paymentMethodRepository
                    .existsPaymentMethodByProviderAndMaskedNumber(
                            newPaymentMethodDto.getProvider(),
                            newPaymentMethodDto.getMaskedNumber());

            if (exists) {
                throw new IllegalArgumentException("Another payment method with this provider and masked number already exists.");
            }
        }

        existing.setProvider(newPaymentMethodDto.getProvider());
        existing.setMaskedNumber(newPaymentMethodDto.getMaskedNumber());
        existing.setDetails(newPaymentMethodDto.getDetails());
        existing.setActive(newPaymentMethodDto.isActive());

        return modelMapper.map(paymentMethodRepository.save(existing), PaymentMethodDto.class);
    }

    @Override
    public void deletePaymentMethod(Long id) {
        if (!paymentMethodRepository.existsById(id)) {
            throw new EntityNotFoundException("Payment method with ID " + id + " not found");
        }
        paymentMethodRepository.deleteById(id);
    }
}

