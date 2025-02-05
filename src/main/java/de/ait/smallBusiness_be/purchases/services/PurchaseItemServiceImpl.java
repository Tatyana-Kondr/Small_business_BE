package de.ait.smallBusiness_be.purchases.services;

import de.ait.smallBusiness_be.products.dao.ProductRepository;
import de.ait.smallBusiness_be.products.model.Product;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicReference;

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
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public PurchaseItemDto createPurchaseItem(NewPurchaseItemDto newPurchaseItemDto, Long purchaseId) {//если нужно добавлять PurchaseItem в уже существующую закупку (Purchase)

        if (purchaseId == null) {
            throw new IllegalArgumentException("Purchase ID must not be null");
        }

        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase not found with ID: " + purchaseId));

        Product product = productRepository.findById(newPurchaseItemDto.getProduct().getId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + newPurchaseItemDto.getProduct().getId()));

        Integer maxPosition = purchaseItemRepository.findMaxPositionByPurchaseId(purchaseId);
        int newPosition = (maxPosition != null) ? maxPosition + 1 : 1;

        PurchaseItem purchaseItem = modelMapper.map(newPurchaseItemDto, PurchaseItem.class);
        purchaseItem.setProduct(product);
        purchaseItem.setPurchase(purchase);
        purchaseItem.setPosition(newPosition);

        // Рассчитываем totalPrice, taxAmount и totalAmount
        BigDecimal totalPrice = purchaseItem.getUnitPrice().multiply(BigDecimal.valueOf(purchaseItem.getQuantity()));
        BigDecimal taxAmount = totalPrice
                .multiply(purchaseItem.getTaxPercentage().movePointLeft(2)) // Это эквивалентно делению на 100.
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = totalPrice.add(taxAmount);

        purchaseItem.setTotalPrice(totalPrice);
        purchaseItem.setTaxAmount(taxAmount);
        purchaseItem.setTotalAmount(totalAmount);

        PurchaseItem savedPurchaseItem = purchaseItemRepository.save(purchaseItem);

        // Пересчитываем итоговые суммы
        recalculatePurchaseTotals(purchase);

        return modelMapper.map(savedPurchaseItem, PurchaseItemDto.class);
    }

    @Override
    public Page<PurchaseItemDto> getAllPurchaseItemsByPurchaseId(Pageable pageable, Long purchaseId) {

        if (!purchaseRepository.existsById(purchaseId)) {
            throw new IllegalArgumentException("Purchase not found with ID: " + purchaseId);
        }
        return purchaseItemRepository.findByPurchaseId(pageable, purchaseId)
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
        modelMapper.map(newPurchaseItemDto, purchaseItem);
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

    private void recalculatePurchaseTotals(Purchase purchase) {
        // Пересчитываем суммы
        AtomicReference<BigDecimal> subtotal = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> taxSum = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> total = new AtomicReference<>(BigDecimal.ZERO);

        // Перебираем все элементы закупки
        purchase.getPurchaseItems().forEach(purchaseItem -> {
            BigDecimal totalPrice = purchaseItem.getUnitPrice().multiply(BigDecimal.valueOf(purchaseItem.getQuantity()));
            BigDecimal taxAmount = totalPrice
                    .multiply(purchaseItem.getTaxPercentage().movePointLeft(2)) // Это эквивалентно делению на 100.
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalAmount = totalPrice.add(taxAmount);

            // Обновляем суммы через AtomicReference
            subtotal.updateAndGet(value -> value.add(totalPrice));
            taxSum.updateAndGet(value -> value.add(taxAmount));
            total.updateAndGet(value -> value.add(totalAmount));
        });

        // Обновляем закупку
        purchase.setSubtotal(subtotal.get());
        purchase.setTaxSum(taxSum.get());
        purchase.setTotal(total.get());

        // Сохраняем обновленную закупку
        purchaseRepository.save(purchase);
    }
}

