package de.ait.smallBusiness_be.productions.services;

import de.ait.smallBusiness_be.productions.dto.NewProductionDto;
import de.ait.smallBusiness_be.productions.dto.ProductionDto;
import de.ait.smallBusiness_be.purchases.dto.PurchaseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ProductionService {

    ProductionDto createProduction(NewProductionDto newProductionDto);
    Page<ProductionDto> getAllProductions(Pageable pageable);
    ProductionDto getProductionById(Long id);
    ProductionDto updateProduction(Long id, NewProductionDto newProductionDto);
    void deleteProduction(Long id);
    Page<ProductionDto> searchProduction(Pageable pageable, String query);
    Page<ProductionDto> getAllProductionsByFilter(Pageable pageable, LocalDate startDate, LocalDate endDate, String searchQuery);
}
