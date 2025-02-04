package de.ait.smallBusiness_be.purchases.services;

import de.ait.smallBusiness_be.purchases.dto.NewPurchaseItemDto;
import de.ait.smallBusiness_be.purchases.dto.PurchaseItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface PurchaseItemService {
    PurchaseItemDto createPurchaseItem(NewPurchaseItemDto newPurchaseItemDto, Long purchaseId);
    Page<PurchaseItemDto> getAllPurchaseItemsByPurchaseId(Pageable pageable, Long purchaseId);
    PurchaseItemDto getPurchaseItemById(Long id);
    PurchaseItemDto updatePurchaseItem(Long id, NewPurchaseItemDto newPurchaseItemDto);
    void deletePurchaseItem(Long id);
}
