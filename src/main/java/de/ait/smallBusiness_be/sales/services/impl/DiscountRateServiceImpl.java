package de.ait.smallBusiness_be.sales.services.impl;

import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.sales.dao.DiscountRateRepository;
import de.ait.smallBusiness_be.sales.dto.DiscountRateDto;
import de.ait.smallBusiness_be.sales.dto.NewDiscountRateDto;
import de.ait.smallBusiness_be.sales.models.DiscountRate;
import de.ait.smallBusiness_be.sales.services.DiscountRateService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscountRateServiceImpl implements DiscountRateService {

    private final DiscountRateRepository discountRateRepository;

    @Override
    public DiscountRateDto createDiscountRate(NewDiscountRateDto newDiscountRateDto) {
        BigDecimal value = newDiscountRateDto.getValue();

        if (discountRateRepository.existsByValue(value)) {
            throw new RestApiException(ErrorDescription.DISCOUNT_RATE_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        boolean active = value.compareTo(BigDecimal.ZERO) == 0 || newDiscountRateDto.isActive();

        DiscountRate discountRate = DiscountRate.builder()
                .value(value)
                .active(active)
                .build();

        return toDto(discountRateRepository.save(discountRate));
    }

    @Override
    public List<DiscountRateDto> getAllDiscountRates() {
        return discountRateRepository
                .findAllByOrderByValueAsc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<DiscountRateDto> getActiveDiscountRates() {
        return discountRateRepository
                .findByActiveTrueOrderByValueAsc()
                .stream()
                .map(this::toDto)
                .toList();
    }


    @Override
    @Transactional
    public DiscountRateDto updateDiscountRate(Long id, NewDiscountRateDto newDiscountRateDto) {

        DiscountRate discountRate = discountRateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.DISCOUNT_RATE_NOT_FOUND.getDescription()));

        if (discountRate.getValue().compareTo(BigDecimal.ZERO) == 0) {
            throw new RestApiException(ErrorDescription.ZERO_DISCOUNT_RATE_CHANGE_FORBIDDEN, HttpStatus.CONFLICT);
        }

        BigDecimal newValue = newDiscountRateDto.getValue();
        boolean valueChanged = discountRate.getValue().compareTo(newValue) != 0;

        if (valueChanged && discountRateRepository.existsByValue(newValue)) {
            throw new RestApiException(ErrorDescription.DISCOUNT_RATE_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        discountRate.setValue(newValue);
        discountRate.setActive(newValue.compareTo(BigDecimal.ZERO) == 0 || newDiscountRateDto.isActive());

        return toDto(discountRateRepository.save(discountRate));
    }


    @Override
    public void deleteDiscountRate(Long id) {
        DiscountRate discountRate = discountRateRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.DISCOUNT_RATE_NOT_FOUND.getDescription()));

        if (discountRate.getValue().compareTo(BigDecimal.ZERO) == 0) {
            throw new RestApiException(ErrorDescription.ZERO_DISCOUNT_RATE_DELETE_FORBIDDEN, HttpStatus.CONFLICT);
        }

        discountRateRepository.delete(discountRate);
    }

    private DiscountRateDto toDto(DiscountRate discountRate) {
        return DiscountRateDto.builder()
                .id(discountRate.getId())
                .value(discountRate.getValue())
                .active(discountRate.isActive())
                .build();
    }
}