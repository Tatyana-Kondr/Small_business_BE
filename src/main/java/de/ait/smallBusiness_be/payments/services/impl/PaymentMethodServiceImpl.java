package de.ait.smallBusiness_be.payments.services.impl;

import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.payments.dao.PaymentMethodRepository;
import de.ait.smallBusiness_be.payments.dao.PaymentRepository;
import de.ait.smallBusiness_be.payments.dto.NewPaymentMethodDto;
import de.ait.smallBusiness_be.payments.dto.PaymentMethodDto;
import de.ait.smallBusiness_be.payments.model.PaymentMethod;
import de.ait.smallBusiness_be.payments.services.PaymentMethodService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final PaymentMethodRepository repository;
    private final PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;

    @Override
    public PaymentMethodDto createPaymentMethod(NewPaymentMethodDto dto) {

        String provider = dto.getProvider().trim();
        String maskedNumber = normalizeOptional(dto.getMaskedNumber());
        String details = normalizeOptional(dto.getDetails());

        if (repository.existsPaymentMethodByProviderAndMaskedNumber(provider, maskedNumber)) {
            throw new RestApiException(ErrorDescription.PAYMENT_METHOD_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        PaymentMethod method = PaymentMethod.builder()
                .provider(provider)
                .maskedNumber(maskedNumber)
                .details(details)
                .active(dto.isActive())
                .build();

        PaymentMethod saved = repository.save(method);

        return modelMapper.map(saved, PaymentMethodDto.class);
    }

    @Override
    public List<PaymentMethodDto> getAllPaymentMethods() {
        return repository.findAll()
                .stream()
                .map(method -> modelMapper.map(method, PaymentMethodDto.class))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethodDto> getActivePaymentMethods() {
        return repository.findAllByActiveTrueOrderByProviderAsc()
                .stream()
                .map(method -> modelMapper.map(method, PaymentMethodDto.class))
                .toList();
    }

    @Override
    public PaymentMethodDto getPaymentMethodById(Long id) {

        PaymentMethod method = getPaymentMethodOrThrow(id);

        return modelMapper.map(method, PaymentMethodDto.class);
    }

    @Override
    public PaymentMethodDto updatePaymentMethod(Long id, NewPaymentMethodDto dto) {

        PaymentMethod existing = getPaymentMethodOrThrow(id);

        String provider = dto.getProvider().trim();
        String maskedNumber = normalizeOptional(dto.getMaskedNumber());
        String details = normalizeOptional(dto.getDetails());

        boolean changed = !existing.getProvider().equals(provider) || !Objects.equals(existing.getMaskedNumber(), maskedNumber);

        if (changed && repository.existsPaymentMethodByProviderAndMaskedNumber(provider, maskedNumber)) {
            throw new RestApiException(ErrorDescription.PAYMENT_METHOD_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        existing.setProvider(provider);
        existing.setMaskedNumber(maskedNumber);
        existing.setDetails(details);
        existing.setActive(dto.isActive());

        PaymentMethod saved = repository.save(existing);

        return modelMapper.map(saved, PaymentMethodDto.class);
    }

    @Override
    @Transactional
    public void deletePaymentMethod(Long id) {

        PaymentMethod method = getPaymentMethodOrThrow(id);

        if (paymentRepository.existsByPaymentMethod_Id(id)) {
            throw new RestApiException(ErrorDescription.PAYMENT_METHOD_IN_USE, HttpStatus.CONFLICT);
        }

        repository.delete(method);
        repository.flush();
    }

    @Override
    public PaymentMethod getPaymentMethodOrThrow(Long id) {

        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.PAYMENT_METHOD_NOT_FOUND.getDescription()));
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
