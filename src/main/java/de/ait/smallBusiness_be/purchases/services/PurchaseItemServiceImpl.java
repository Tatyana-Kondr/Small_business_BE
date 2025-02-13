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
import org.hibernate.Hibernate;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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

        recalculatePurchaseItemTotals(purchaseItem);

        PurchaseItem savedPurchaseItem = purchaseItemRepository.save(purchaseItem);

        recalculatePurchaseTotals(purchase);

        return modelMapper.map(savedPurchaseItem, PurchaseItemDto.class);
    }

    @Override
    public List<PurchaseItemDto> getAllPurchaseItemsByPurchaseId(Long purchaseId) {

        if (!purchaseRepository.existsById(purchaseId)) {
            throw new IllegalArgumentException("Purchase not found with ID: " + purchaseId);
        }
        return purchaseItemRepository.findByPurchaseId(purchaseId)
                .map(item -> modelMapper.map(item, PurchaseItemDto.class)).toList();
    }

    @Override
    public PurchaseItemDto getPurchaseItemById(Long id) {
        PurchaseItem purchaseItem =  purchaseItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Purchase item not found with ID: " + id));
        return modelMapper.map(purchaseItem, PurchaseItemDto.class);
    }

    @Override
    @Transactional
    public PurchaseItemDto updatePurchaseItem(Long id, NewPurchaseItemDto newPurchaseItemDto) {

        PurchaseItem purchaseItem = purchaseItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Purchase item not found with ID: " + id));

        modelMapper.map(newPurchaseItemDto, purchaseItem);

        recalculatePurchaseItemTotals(purchaseItem);
        PurchaseItem updatedPurchaseItem = purchaseItemRepository.save(purchaseItem);
        recalculatePurchaseTotals(purchaseItem.getPurchase());

        return modelMapper.map(updatedPurchaseItem, PurchaseItemDto.class);
    }

    @Override
    @Transactional
    public void deletePurchaseItem(Long id) {

        // Получаем PurchaseItem
        PurchaseItem purchaseItem = purchaseItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Purchase item not found with ID: " + id));

        Purchase purchase = purchaseItem.getPurchase();
        Hibernate.initialize(purchase.getPurchaseItems()); // Загружаем коллекцию перед удалением

        // Удаляем PurchaseItem из коллекции Purchase
        purchase.getPurchaseItems().remove(purchaseItem);

        // Удаляем PurchaseItem из базы
        purchaseItemRepository.delete(purchaseItem);

        // Обновляем позиции оставшихся элементов
        updatePositions(purchase);

        // Пересчитываем суммы в Purchase
        recalculatePurchaseTotals(purchase);

        // Сохраняем Purchase после изменений
        purchaseRepository.save(purchase);
    }

    private void recalculatePurchaseItemTotals(PurchaseItem purchaseItem) {
        BigDecimal totalPrice = purchaseItem.getUnitPrice().multiply(BigDecimal.valueOf(purchaseItem.getQuantity()));
        BigDecimal taxAmount = totalPrice
                .multiply(purchaseItem.getTaxPercentage().movePointLeft(2)) // Это эквивалентно делению на 100.
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = totalPrice.add(taxAmount);

        purchaseItem.setTotalPrice(totalPrice);
        purchaseItem.setTaxAmount(taxAmount);
        purchaseItem.setTotalAmount(totalAmount);
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

    private void updatePositions(Purchase purchase) {
        List<PurchaseItem> items = purchase.getPurchaseItems()
                .stream()
                .sorted(Comparator.comparing(PurchaseItem::getPosition)) // Сортируем по позиции
                .collect(Collectors.toList());

        // Пронумеровываем заново
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setPosition(i + 1); // Начинаем с 1
        }
    }
}

