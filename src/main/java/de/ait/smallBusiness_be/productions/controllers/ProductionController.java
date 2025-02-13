package de.ait.smallBusiness_be.productions.controllers;

import de.ait.smallBusiness_be.productions.controllers.api.ProductionsApi;
import de.ait.smallBusiness_be.productions.dto.NewProductionDto;
import de.ait.smallBusiness_be.productions.dto.ProductionDto;
import de.ait.smallBusiness_be.productions.services.ProductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 13.02.2025
 * SmB_be
 *
 * @author Kondratyeva (AIT TR)
 */

@RestController
@RequiredArgsConstructor
public class ProductionController implements ProductionsApi {

    private final ProductionService productionService;

    @Override
    public ProductionDto addProduction(NewProductionDto newProductionDto) {
        return productionService.createProduction(newProductionDto);
    }

    @Override
    public Page<ProductionDto> getAllProductions(Pageable pageable) {
        return productionService.getAllProductions(pageable);
    }

    @Override
    public ProductionDto getProductionById(Long id) {
        return productionService.getProductionById(id);
    }

    @Override
    public ProductionDto updateProduction(Long id, NewProductionDto newProductionDto) {
        return productionService.updateProduction(id, newProductionDto);
    }

    @Override
    public void removeProduction(Long id) {
        productionService.deleteProduction(id);
    }
}
