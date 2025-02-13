package de.ait.smallBusiness_be.productions.services;

import de.ait.smallBusiness_be.productions.dto.NewProductionDto;
import de.ait.smallBusiness_be.productions.dto.ProductionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * 13.02.2025
 * SmB_be
 *
 * @author Kondratyeva (AIT TR)
 */

@Service
@RequiredArgsConstructor
public class ProductionServiceImpl implements ProductionService{
    @Override
    public ProductionDto createProduction(NewProductionDto newProductionDto) {
        return null;
    }

    @Override
    public Page<ProductionDto> getAllProductions(Pageable pageable) {
        return null;
    }

    @Override
    public ProductionDto getProductionById(Long id) {
        return null;
    }

    @Override
    public ProductionDto updateProduction(Long id, NewProductionDto newProductionDto) {
        return null;
    }

    @Override
    public void deleteProduction(Long id) {

    }
}
