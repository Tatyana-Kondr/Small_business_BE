package de.ait.smallBusiness_be.sales.services.impl;

import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.sales.dao.TaxRateRepository;
import de.ait.smallBusiness_be.sales.dto.NewTaxRateDto;
import de.ait.smallBusiness_be.sales.dto.TaxRateDto;
import de.ait.smallBusiness_be.sales.models.TaxRate;
import de.ait.smallBusiness_be.sales.services.TaxRateService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaxRateServiceImpl implements TaxRateService {

    private final TaxRateRepository taxRateRepository;
    private final ModelMapper modelMapper;



    @Override
    @Transactional
    public TaxRateDto createTaxRate(NewTaxRateDto newTaxRateDto) {

        BigDecimal value = newTaxRateDto.getValue().stripTrailingZeros();

        if (taxRateRepository.existsByValue(value)) {
            throw new RestApiException(ErrorDescription.TAX_RATE_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        boolean firstRate = taxRateRepository.count() == 0;
        boolean defaultRate = firstRate || newTaxRateDto.isDefaultRate();
        boolean active = defaultRate || newTaxRateDto.isActive();

        if (defaultRate && !firstRate) {
            clearCurrentDefault(null);
        }

        TaxRate taxRate = TaxRate.builder()
                .value(value)
                .active(active)
                .defaultRate(defaultRate)
                .build();

        TaxRate saved = taxRateRepository.save(taxRate);

        return modelMapper.map(saved, TaxRateDto.class);
    }

    @Override
    public List<TaxRateDto> getAllTaxRates() {
        return taxRateRepository
                .findAllByOrderByValueAsc()
                .stream()
                .map(taxRate -> modelMapper.map(taxRate, TaxRateDto.class))
                .toList();
    }

    @Override
    public List<TaxRateDto> getActiveTaxRates() {
        return taxRateRepository
                .findByActiveTrueOrderByValueAsc()
                .stream()
                .map(taxRate -> modelMapper.map(taxRate, TaxRateDto.class))
                .toList();
    }

    @Override
    public TaxRateDto getDefaultTaxRate() {
        TaxRate taxRate = taxRateRepository
                .findByDefaultRateTrue()
                .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.DEFAULT_TAX_RATE_NOT_FOUND.getDescription()));

        return modelMapper.map(taxRate, TaxRateDto.class);
    }



    @Override
    @Transactional
    public TaxRateDto updateTaxRate(Long id, NewTaxRateDto newTaxRateDto) {
        TaxRate existing = taxRateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.TAX_RATE_NOT_FOUND.getDescription()));

        BigDecimal value = newTaxRateDto.getValue().stripTrailingZeros();

        if (existing.getValue().compareTo(value) != 0 && taxRateRepository.existsByValue(value)) {
            throw new RestApiException(ErrorDescription.TAX_RATE_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        if (existing.isDefaultRate() && !newTaxRateDto.isDefaultRate()) {
            throw new RestApiException(ErrorDescription.DEFAULT_TAX_RATE_CHANGE_FORBIDDEN, HttpStatus.CONFLICT);
        }

        boolean defaultRate = newTaxRateDto.isDefaultRate();
        boolean active = defaultRate || newTaxRateDto.isActive();

        if (defaultRate) {
            clearCurrentDefault(id);
        }

        existing.setValue(value);
        existing.setActive(active);
        existing.setDefaultRate(defaultRate);

        TaxRate saved = taxRateRepository.save(existing);

        return modelMapper.map(saved, TaxRateDto.class);
    }

    @Override
    @Transactional
    public void deleteTaxRate(Long id) {
        TaxRate taxRate = taxRateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.TAX_RATE_NOT_FOUND.getDescription()));

        if (taxRate.isDefaultRate()) {
            throw new RestApiException(ErrorDescription.DEFAULT_TAX_RATE_DELETE_FORBIDDEN, HttpStatus.CONFLICT);
        }
        taxRateRepository.delete(taxRate);
    }

    private void clearCurrentDefault(Long excludedId) {
        taxRateRepository
                .findByDefaultRateTrue()
                .ifPresent(currentDefault -> {
                    if (excludedId == null || !currentDefault.getId().equals(excludedId)) {
                        currentDefault.setDefaultRate(false);
                        taxRateRepository.save(currentDefault);
                    }
                });
    }
}
