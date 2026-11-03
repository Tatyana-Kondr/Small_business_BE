package de.ait.smallBusiness_be.sales.controllers;

import de.ait.smallBusiness_be.sales.controllers.api.DiscountRateApi;
import de.ait.smallBusiness_be.sales.dto.DiscountRateDto;
import de.ait.smallBusiness_be.sales.dto.NewDiscountRateDto;
import de.ait.smallBusiness_be.sales.services.DiscountRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DiscountRateController implements DiscountRateApi {

    private final DiscountRateService discountRateService;

    @Override
    public DiscountRateDto createDiscountRate(
            NewDiscountRateDto newDiscountRateDto
    ) {
        return discountRateService.createDiscountRate(newDiscountRateDto);
    }

    @Override
    public List<DiscountRateDto> getAllDiscountRates() {
        return discountRateService.getAllDiscountRates();
    }

    @Override
    public List<DiscountRateDto> getActiveDiscountRates() {
        return discountRateService.getActiveDiscountRates();
    }

    @Override
    public DiscountRateDto updateDiscountRate(
            Long id,
            NewDiscountRateDto newDiscountRateDto
    ) {
        return discountRateService.updateDiscountRate(
                id,
                newDiscountRateDto
        );
    }

    @Override
    public void deleteDiscountRate(Long id) {
        discountRateService.deleteDiscountRate(id);
    }
}