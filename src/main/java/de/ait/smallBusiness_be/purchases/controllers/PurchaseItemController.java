package de.ait.smallBusiness_be.purchases.controllers;

import de.ait.smallBusiness_be.purchases.controllers.api.PurchaseItemsApi;
import de.ait.smallBusiness_be.purchases.dto.NewPurchaseItemDto;
import de.ait.smallBusiness_be.purchases.dto.PurchaseItemDto;
import de.ait.smallBusiness_be.purchases.services.PurchaseItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 17.01.2025
 * SmB
 *
 * @author Kondratyeva (AIT TR)
 */

@RestController
@RequiredArgsConstructor
public class PurchaseItemController implements PurchaseItemsApi {

    private final PurchaseItemService purchaseItemService;

    @Override
    public PurchaseItemDto addPurchaseItem(NewPurchaseItemDto newPurchaseItemDto, Long purchaseId) {
        return purchaseItemService.createPurchaseItem(newPurchaseItemDto, purchaseId);
    }

    @Override
    public List<PurchaseItemDto> getAllPurchaseItemsByPurchaseId(Long purchaseId) {
        return purchaseItemService.getAllPurchaseItemsByPurchaseId(purchaseId);
    }

    @Override
    public PurchaseItemDto getPurchaseItemById(Long id) {
        return purchaseItemService.getPurchaseItemById(id);
    }

    @Override
    public PurchaseItemDto updatePurchaseItem(Long id, NewPurchaseItemDto newPurchaseItemDto) {
        return purchaseItemService.updatePurchaseItem(id, newPurchaseItemDto);
    }

    @Override
    public void deletePurchaseItem(Long id) { purchaseItemService.deletePurchaseItem(id); }
}
