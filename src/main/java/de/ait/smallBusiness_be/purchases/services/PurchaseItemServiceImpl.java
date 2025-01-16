package de.ait.smallBusiness_be.purchases.services;

import de.ait.smallBusiness_be.purchases.dao.PurchaseItemRepository;
import de.ait.smallBusiness_be.purchases.dao.PurchaseRepository;
import de.ait.smallBusiness_be.purchases.dto.NewPurchaseItemDto;
import de.ait.smallBusiness_be.purchases.dto.PurchaseItemDto;
import de.ait.smallBusiness_be.purchases.model.Purchase;
import de.ait.smallBusiness_be.purchases.model.PurchaseItem;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * 16.01.2025
 * SmB
 *
 * @author Kondratyeva (AIT TR)
 */

@Service
@RequiredArgsConstructor
public class PurchaseItemServiceImpl implements PurchaseItemService {

    private final PurchaseItemRepository purchaseItemRepository;
    private final PurchaseRepository purchaseRepository;
    private final ModelMapper modelMapper;

    @Override
    public PurchaseItemDto createPurchaseItem(NewPurchaseItemDto newPurchaseItemDto) {

        Purchase purchase = purchaseRepository.findById(newPurchaseItemDto.getPurchaseId())
                .orElseThrow(() -> new IllegalArgumentException("Purchase not found with ID: " + newPurchaseItemDto.getPurchaseId()));
        PurchaseItem purchaseItem = modelMapper.map(newPurchaseItemDto, PurchaseItem.class);
        purchaseItem.setPurchase(purchase);
        PurchaseItem savedPurchaseItem = purchaseItemRepository.save(purchaseItem);
        return modelMapper.map(savedPurchaseItem, PurchaseItemDto.class);
    }

    @Override
    public Page<PurchaseItemDto> getAllPurchaseItemsByPurchaseId(Pageable pageable, Long id) {

        if (!purchaseRepository.existsById(id)) {
            throw new IllegalArgumentException("Purchase not found with ID: " + id);
        }
        return purchaseItemRepository.findByPurchaseId(pageable, id)
                .map(item -> modelMapper.map(item, PurchaseItemDto.class));
    }

    @Override
    public PurchaseItemDto getPurchaseItemById(Long id) {
        PurchaseItem purchaseItem =  purchaseItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Purchase item not found with ID: " + id));
        return modelMapper.map(purchaseItem, PurchaseItemDto.class);
    }

    @Override
    public PurchaseItemDto updatePurchaseItem(Long id, NewPurchaseItemDto newPurchaseItemDto) {
        PurchaseItem purchaseItem =  purchaseItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Purchase item not found with ID: " + id));
        Purchase purchase = purchaseRepository.findById(newPurchaseItemDto.getPurchaseId())
                .orElseThrow(() -> new IllegalArgumentException("Purchase not found with ID: " + newPurchaseItemDto.getPurchaseId()));

        modelMapper.map(newPurchaseItemDto, purchaseItem);
        purchaseItem.setPurchase(purchase);
        PurchaseItem updatedPurchaseItem = purchaseItemRepository.save(purchaseItem);

        return modelMapper.map(updatedPurchaseItem, PurchaseItemDto.class);
    }

    @Override
    public void deletePurchaseItem(Long id) {

        if (!purchaseItemRepository.existsById(id)) {
            throw new IllegalArgumentException("Purchase item not found with ID: " + id);
        }
        purchaseItemRepository.deleteById(id);
    }
}
