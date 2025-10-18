package de.ait.smallBusiness_be.sales.services.impl;

import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.products.service.ProductService;
import de.ait.smallBusiness_be.purchases.dto.PurchaseItemDto;
import de.ait.smallBusiness_be.purchases.model.Purchase;
import de.ait.smallBusiness_be.purchases.model.PurchaseItem;
import de.ait.smallBusiness_be.sales.dao.SaleItemRepository;
import de.ait.smallBusiness_be.sales.dao.SaleRepository;
import de.ait.smallBusiness_be.sales.dto.NewSaleItemDto;
import de.ait.smallBusiness_be.sales.dto.SaleItemDto;
import de.ait.smallBusiness_be.sales.models.Sale;
import de.ait.smallBusiness_be.sales.models.SaleItem;
import de.ait.smallBusiness_be.sales.services.SaleItemService;
import de.ait.smallBusiness_be.sales.services.SaleService;
import jakarta.persistence.EntityNotFoundException;
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
 * 2/5/2025
 * Small_business_BE
 *
 * @author Chechkina (AIT TR)
 */

@RequiredArgsConstructor
@Service
public class SaleItemServiceImpl implements SaleItemService {

    private final SaleItemRepository saleItemRepository;
    private final SaleService saleService;
    private final ProductService productService;
    private final ModelMapper modelMapper;
    private final SaleRepository saleRepository;

    @Override
    @Transactional
    public SaleItemDto createSaleItem(Long saleId, NewSaleItemDto newSaleItem) {

        Sale sale = saleService.getSaleOrThrow(saleId);

        Product product = productService.getProductOrThrow(newSaleItem.getProductId());

        Integer maxPosition = saleItemRepository.findMaxPositionBySaleId(saleId);
        int newPosition = (maxPosition != null) ? maxPosition + 1 : 1;

        SaleItem saleItem = modelMapper.map(newSaleItem, SaleItem.class);
        saleItem.setProduct(product);
        saleItem.setSale(sale);
        saleItem.setPosition(newPosition);
        recalculateSaleItemTotals(saleItem);

        SaleItem savedItem = saleItemRepository.save(saleItem);

        recalculateSaleTotals(sale);

        return modelMapper.map(savedItem, SaleItemDto.class);
    }

    @Override
    public List<SaleItemDto> getAllSaleItemsBySaleId(Long saleId) {

        if (!saleService.checkIfSaleExistsById(saleId)) {
            throw new IllegalArgumentException("Sale with ID: " + saleId + " does not exist");
        }
        return saleItemRepository
                .findAllBySaleIdOrderByPosition(saleId)
                .stream()
                .map(item -> modelMapper.map(item, SaleItemDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public SaleItemDto getSaleItemById(Long saleId, Long saleItemId) {
        SaleItem saleItem = saleItemRepository.findById(saleItemId)
                .orElseThrow(() -> new EntityNotFoundException("SaleItem with id " + saleItemId + " not found"));

        return modelMapper.map(saleItem, SaleItemDto.class);
    }

    @Override
    public SaleItemDto updateSaleItem(Long saleId, Long saleItemId, NewSaleItemDto updateSaleItem) {

        SaleItem saleItem = saleItemRepository.findById(saleItemId)
                .orElseThrow(() -> new IllegalArgumentException("SaleItem with id " + saleItemId + " not found"));

        // Обновляем количество и цену
        if (updateSaleItem.getQuantity() != null) {
            saleItem.setQuantity(updateSaleItem.getQuantity());
        }
        if (updateSaleItem.getUnitPrice() != null) {
            saleItem.setUnitPrice(updateSaleItem.getUnitPrice());
        }

        if (updateSaleItem.getProductName() != null) {
            saleItem.setProductName(updateSaleItem.getProductName());
        }

        // Скидка и налог: либо из DTO, либо дефолтные
        Sale sale = saleItem.getSale();
        saleItem.setDiscount(updateSaleItem.getDiscount() != null ? updateSaleItem.getDiscount() : sale.getDefaultDiscount());
        saleItem.setTax(updateSaleItem.getTax() != null ? updateSaleItem.getTax() : sale.getDefaultTax());

        // Пересчитываем суммы
        recalculateSaleItemTotals(saleItem);

        SaleItem updatedItem = saleItemRepository.save(saleItem);

        // Пересчёт итогов всей продажи
        recalculateSaleTotals(sale);

        return modelMapper.map(updatedItem, SaleItemDto.class);
    }



    @Override
    public void deleteSaleItem(Long saleId, Long saleItemId) {
        // Получаем PurchaseItem
        SaleItem saleItem = saleItemRepository.findById(saleItemId)
                .orElseThrow(() -> new IllegalArgumentException("SaleItem with id " + saleItemId + " not found"));

        Sale sale = saleItem.getSale();
        Hibernate.initialize(sale.getSaleItems());

        sale.getSaleItems().remove(saleItem);
        // Удаляем из базы
        saleItemRepository.delete(saleItem);

        updatePositions(sale);
        // Пересчитываем суммы в Sale
        recalculateSaleTotals(sale);

    }

    private void recalculateSaleItemTotals(SaleItem saleItem) {
        BigDecimal totalPriceWithoutDiscount = saleItem.getUnitPrice().multiply(saleItem.getQuantity());
        BigDecimal discountAmount = totalPriceWithoutDiscount
                .multiply(saleItem.getDiscount().movePointLeft(2))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPrice = totalPriceWithoutDiscount.subtract(discountAmount);
        BigDecimal taxAmount = totalPrice
                .multiply(saleItem.getTax().movePointLeft(2))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = totalPrice.add(taxAmount);

        saleItem.setDiscountAmount(discountAmount);
        saleItem.setTotalPrice(totalPrice);
        saleItem.setTaxAmount(taxAmount);
        saleItem.setTotalAmount(totalAmount);
    }

    private void recalculateSaleTotals(Sale sale) {
        AtomicReference<BigDecimal> subtotal = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> discountSum = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> taxSum = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> total = new AtomicReference<>(BigDecimal.ZERO);

        sale.getSaleItems().forEach(item -> {
            subtotal.updateAndGet(v -> v.add(
                    item.getTotalPrice() != null ? item.getTotalPrice() : BigDecimal.ZERO));
            discountSum.updateAndGet(v -> v.add(
                    item.getDiscountAmount() != null ? item.getDiscountAmount() : BigDecimal.ZERO));
            taxSum.updateAndGet(v -> v.add(
                    item.getTaxAmount() != null ? item.getTaxAmount() : BigDecimal.ZERO));
            total.updateAndGet(v -> v.add(
                    item.getTotalAmount() != null ? item.getTotalAmount() : BigDecimal.ZERO));
        });

        sale.setTotalPrice(subtotal.get());
        sale.setDiscountAmount(discountSum.get());
        sale.setTaxAmount(taxSum.get());
        sale.setTotalAmount(total.get());

        saleRepository.save(sale);
    }

    private void updatePositions(Sale sale) {
        List<SaleItem> items = sale.getSaleItems().stream()
                .sorted(Comparator.comparing(SaleItem::getPosition))
                .collect(Collectors.toList());
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setPosition(i + 1);
        }
    }
}