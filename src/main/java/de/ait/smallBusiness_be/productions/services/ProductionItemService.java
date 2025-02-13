package de.ait.smallBusiness_be.productions.services;

import de.ait.smallBusiness_be.productions.dto.NewProductionItemDto;
import de.ait.smallBusiness_be.productions.dto.ProductionItemDto;


public interface ProductionItemService {
    ProductionItemDto createProductionItem(NewProductionItemDto newProductionItemDto, Long productionId);
    ProductionItemDto getProductionItemById(Long id);
    ProductionItemDto updateProductionItem(Long id, NewProductionItemDto newProductionItemDto);
    void deleteProductionItem(Long id);
}
