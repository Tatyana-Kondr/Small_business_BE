package de.ait.smallBusiness_be.productions.services;

import de.ait.smallBusiness_be.productions.dto.NewProductionItemDto;
import de.ait.smallBusiness_be.productions.dto.ProductionItemDto;
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
public class ProductionItemServiceImpl implements ProductionItemService {

    @Override
    public ProductionItemDto createProductionItem(NewProductionItemDto newProductionItemDto) {
        return null;
    }

    @Override
    public Page<ProductionItemDto> getAllProductionItems(Pageable pageable) {
        return null;
    }

    @Override
    public ProductionItemDto getProductionItemById(Long id) {
        return null;
    }

    @Override
    public ProductionItemDto updateProductionItem(Long id, NewProductionItemDto newProductionItemDto) {
        return null;
    }

    @Override
    public void deleteProductionItem(Long id) {

    }
}
