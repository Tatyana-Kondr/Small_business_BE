package de.ait.smallBusiness_be.productions.services;

import de.ait.smallBusiness_be.productions.dto.NewProductionDto;
import de.ait.smallBusiness_be.productions.dto.ProductionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductionService {

    ProductionDto createProduction(NewProductionDto newProductionDto);
    Page<ProductionDto> getAllProductions(Pageable pageable);
    ProductionDto getProductionById(Long id);
    ProductionDto updateProduction(Long id, NewProductionDto newProductionDto);
    void deleteProduction(Long id);
}
