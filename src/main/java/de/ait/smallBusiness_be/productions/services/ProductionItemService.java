package de.ait.smallBusiness_be.productions.services;

import de.ait.smallBusiness_be.productions.dto.NewProductionItemDto;
import de.ait.smallBusiness_be.productions.dto.ProductionItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductionItemService {
    ProductionItemDto createProductionItem(NewProductionItemDto newProductionItemDto);
    Page<ProductionItemDto> getAllProductionItems(Pageable pageable);
    ProductionItemDto getProductionItemById(Long id);
    ProductionItemDto updateProductionItem(Long id, NewProductionItemDto newProductionItemDto);
    void deleteProductionItem(Long id);
}
