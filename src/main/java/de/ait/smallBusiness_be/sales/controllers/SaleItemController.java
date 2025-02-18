package de.ait.smallBusiness_be.sales.controllers;

import de.ait.smallBusiness_be.sales.controllers.api.SaleItemApi;
import de.ait.smallBusiness_be.sales.dto.NewSaleItemDto;
import de.ait.smallBusiness_be.sales.dto.SaleItemDto;
import de.ait.smallBusiness_be.sales.services.SaleItemService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 2/7/2025
 * Small_business_BE
 *
 * @author Chechkina (AIT TR)
 */

@RestController
@RequestMapping("/api/saleItems")
@RequiredArgsConstructor
@Tag(name = "Sale Item Controller")
public class SaleItemController implements SaleItemApi {

    private final SaleItemService saleItemService;

    @Override
    public SaleItemDto addSaleItem(NewSaleItemDto newSaleItemDto, Long saleId) {
        return saleItemService.createSaleItem(saleId, newSaleItemDto);
    }

    @Override
    public List<SaleItemDto> getAllSaleItemsBySaleId(Long saleId) {
        return saleItemService.getAllSaleItemsBySaleId(saleId);
    }

    @Override
    public SaleItemDto getSaleItemById(Long saleId, Long saleItemId) {
        return saleItemService.getSaleItemById(saleId, saleItemId);
    }

    @Override
    public SaleItemDto updateSaleItem(Long saleId, Long saleItemId, NewSaleItemDto newSaleItemDto) {
        return saleItemService.updateSaleItem(saleId, saleItemId, newSaleItemDto);
    }

    @Override
    public void deleteSaleItem(Long saleId, Long saleItemId) {
    }
}
