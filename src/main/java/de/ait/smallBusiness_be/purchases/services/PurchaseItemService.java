package de.ait.smallBusiness_be.purchases.services;

import de.ait.smallBusiness_be.purchases.dto.NewPurchaseItemDto;
import de.ait.smallBusiness_be.purchases.dto.PurchaseItemDto;


import java.util.List;


public interface PurchaseItemService {
    PurchaseItemDto createPurchaseItem(NewPurchaseItemDto newPurchaseItemDto, Long purchaseId);
    List<PurchaseItemDto> getAllPurchaseItemsByPurchaseId(Long purchaseId);
    PurchaseItemDto getPurchaseItemById(Long id);
    PurchaseItemDto updatePurchaseItem(Long id, NewPurchaseItemDto newPurchaseItemDto);
    void deletePurchaseItem(Long id);
}
