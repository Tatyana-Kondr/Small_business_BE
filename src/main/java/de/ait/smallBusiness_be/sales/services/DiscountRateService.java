package de.ait.smallBusiness_be.sales.services;

import de.ait.smallBusiness_be.sales.dto.DiscountRateDto;
import de.ait.smallBusiness_be.sales.dto.NewDiscountRateDto;

import java.util.List;

public interface DiscountRateService {

    DiscountRateDto createDiscountRate(NewDiscountRateDto newDiscountRateDto);

    List<DiscountRateDto> getAllDiscountRates();

    List<DiscountRateDto> getActiveDiscountRates();

    DiscountRateDto updateDiscountRate(
            Long id,
            NewDiscountRateDto newDiscountRateDto
    );

    void deleteDiscountRate(Long id);
}