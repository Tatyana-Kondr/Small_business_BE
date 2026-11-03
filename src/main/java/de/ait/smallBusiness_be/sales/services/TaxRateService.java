package de.ait.smallBusiness_be.sales.services;

import de.ait.smallBusiness_be.sales.dto.NewTaxRateDto;
import de.ait.smallBusiness_be.sales.dto.TaxRateDto;

import java.util.List;

public interface TaxRateService {

    TaxRateDto createTaxRate(NewTaxRateDto newTaxRateDto);

    List<TaxRateDto> getAllTaxRates();

    List<TaxRateDto> getActiveTaxRates();

    TaxRateDto getDefaultTaxRate();

    TaxRateDto updateTaxRate(
            Long id,
            NewTaxRateDto newTaxRateDto
    );

    void deleteTaxRate(Long id);
}
