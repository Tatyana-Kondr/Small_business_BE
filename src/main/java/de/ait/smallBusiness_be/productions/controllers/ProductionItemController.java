package de.ait.smallBusiness_be.productions.controllers;

import de.ait.smallBusiness_be.productions.controllers.api.ProductionItemsApi;
import de.ait.smallBusiness_be.productions.dto.NewProductionItemDto;
import de.ait.smallBusiness_be.productions.dto.ProductionItemDto;
import de.ait.smallBusiness_be.productions.services.ProductionItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 13.02.2025
 * SmB_be
 *
 * @author Kondratyeva (AIT TR)
 */

@RestController
@RequiredArgsConstructor
public class ProductionItemController implements ProductionItemsApi {

    private final ProductionItemService productionItemService;

    @Override
    public ProductionItemDto addProductionItem(NewProductionItemDto newProductionItemDto, Long productionId) {
        return productionItemService.createProductionItem(newProductionItemDto, productionId);
    }

    @Override
    public ProductionItemDto getProductionItemById(Long id) {
        return productionItemService.getProductionItemById(id);
    }

    @Override
    public ProductionItemDto updateProductionItem(Long id, NewProductionItemDto newProductionItemDto) {
        return productionItemService.updateProductionItem(id, newProductionItemDto);
    }

    @Override
    public void removeProductionItem(Long id) {
        productionItemService.deleteProductionItem(id);
    }
}
