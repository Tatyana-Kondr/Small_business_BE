package de.ait.smallBusiness_be.sales.services;

import de.ait.smallBusiness_be.sales.dto.NewSaleItemDto;
import de.ait.smallBusiness_be.sales.dto.SaleItemDto;

import java.util.List;

public interface SaleItemService {

    SaleItemDto createSaleItem(Long saleId, NewSaleItemDto newSaleItem);

    List<SaleItemDto> getAllSaleItemsBySaleId(Long saleId);

    SaleItemDto getSaleItemById(Long saleId, Long saleItemId);

    SaleItemDto updateSaleItem(Long saleId, Long saleItemId, NewSaleItemDto updateSaleItem);

    void deleteSaleItem(Long saleId, Long saleItemId);
}
