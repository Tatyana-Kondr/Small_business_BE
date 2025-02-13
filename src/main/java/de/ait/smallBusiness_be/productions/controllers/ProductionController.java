package de.ait.smallBusiness_be.productions.controllers;

import de.ait.smallBusiness_be.productions.controllers.api.ProductionsApi;
import de.ait.smallBusiness_be.productions.dto.NewProductionDto;
import de.ait.smallBusiness_be.productions.dto.ProductionDto;
import de.ait.smallBusiness_be.productions.services.ProductionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 13.02.2025
 * SmB_be
 *
 * @author Kondratyeva (AIT TR)
 */
public class ProductionController implements ProductionsApi {

    @Override
    public ProductionDto addProduction(NewProductionDto newProductionDto) {
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
    public void removePurchase(Long id) {

    }
}
