package de.ait.smallBusiness_be.sales.controllers;

import de.ait.smallBusiness_be.sales.controllers.api.TaxRateApi;
import de.ait.smallBusiness_be.sales.dto.NewTaxRateDto;
import de.ait.smallBusiness_be.sales.dto.TaxRateDto;
import de.ait.smallBusiness_be.sales.services.TaxRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TaxRateController implements TaxRateApi {

    private final TaxRateService taxRateService;

    @Override
    public TaxRateDto addTaxRate(
            NewTaxRateDto newTaxRateDto
    ) {
        return taxRateService.createTaxRate(
                newTaxRateDto
        );
    }

    @Override
    public List<TaxRateDto> getAllTaxRates() {
        return taxRateService.getAllTaxRates();
    }

    @Override
    public List<TaxRateDto> getActiveTaxRates() {
        return taxRateService.getActiveTaxRates();
    }

    @Override
    public TaxRateDto getDefaultTaxRate() {
        return taxRateService.getDefaultTaxRate();
    }

    @Override
    public TaxRateDto updateTaxRate(
            Long id,
            NewTaxRateDto newTaxRateDto
    ) {
        return taxRateService.updateTaxRate(
                id,
                newTaxRateDto
        );
    }

    @Override
    public void deleteTaxRate(Long id) {
        taxRateService.deleteTaxRate(id);
    }
}