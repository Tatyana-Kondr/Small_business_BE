package de.ait.smallBusiness_be.productions.services;

import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.productions.dao.ProductionItemRepository;
import de.ait.smallBusiness_be.productions.dao.ProductionRepository;
import de.ait.smallBusiness_be.productions.dto.NewProductionDto;
import de.ait.smallBusiness_be.productions.dto.NewProductionItemDto;
import de.ait.smallBusiness_be.productions.dto.ProductionDto;
import de.ait.smallBusiness_be.productions.model.Production;
import de.ait.smallBusiness_be.productions.model.ProductionItem;
import de.ait.smallBusiness_be.products.dao.ProductRepository;
import de.ait.smallBusiness_be.products.model.Product;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 13.02.2025
 * SmB_be
 *
 * @author Kondratyeva (AIT TR)
 */

@Service
@RequiredArgsConstructor
public class ProductionServiceImpl implements ProductionService{

    private final ProductionRepository productionRepository;
    private final ProductionItemRepository productionItemRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public ProductionDto createProduction(NewProductionDto newProductionDto) {
        Product product = productRepository.findById(newProductionDto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + newProductionDto.getProductId()));
        Production production = modelMapper.map(newProductionDto, Production.class);
        production.setProduct(product);
        production.setAmount(newProductionDto.getUnitPrice().multiply(BigDecimal.valueOf(newProductionDto.getQuantity())));

        if(newProductionDto.getProductionItems() != null && !newProductionDto.getProductionItems().isEmpty()) {
            List<ProductionItem> productionItems = newProductionDto.getProductionItems().stream()
                    .map(newProductionItemDto -> {
                        Product productForItem = productRepository.findById(newProductionItemDto.getProductId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                        "Product not found with ID: " + newProductionItemDto.getProductId()
                                ));
                        ProductionItem productionItem = modelMapper.map(newProductionItemDto, ProductionItem.class);
                        productionItem.setProduct(productForItem);
                        productionItem.setProduction(production);
                        BigDecimal totalPrice = productionItem.getUnitPrice().multiply(BigDecimal.valueOf(productionItem.getQuantity()));
                        productionItem.setTotalPrice(totalPrice);

                        return productionItem;
                    })
                    .toList();

            production.setProductionItems(productionItems);
        }
            Production savedProduction = productionRepository.save(production);

        return modelMapper.map(savedProduction, ProductionDto.class);
    }

    @Override
    @Transactional
    public Page<ProductionDto> getAllProductions(Pageable pageable) {
        // Разрешённые поля для сортировки
        List<String> allowedSortFields = List.of("dateOfProduction");

        Sort sort = pageable.getSort();
        boolean hasValidSortField = sort.stream()
                .allMatch(order -> allowedSortFields.contains(order.getProperty()));

        // Если передано некорректное поле сортировки или сортировки вообще нет, заменяем её на дефолтную
        if (!hasValidSortField || sort.isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "dateOfProduction"));
        }

        Page<Production> productions = productionRepository.findAll(pageable);

        if (productions.isEmpty()) {
            throw new RestApiException(ErrorDescription.LIST_IS_EMPTY, HttpStatus.NOT_FOUND);
        }

        return productions.map(production -> modelMapper.map(production, ProductionDto.class));
    }

    @Override
    @Transactional
    public ProductionDto getProductionById(Long id) {
        Production production = productionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Production not found with ID: " + id));
        Hibernate.initialize(production.getProductionItems());
        return modelMapper.map(production, ProductionDto.class);
    }

    @Override
    @Transactional
    public ProductionDto updateProduction(Long id, NewProductionDto newProductionDto) {
        Production production = productionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Production not found with ID: " + id));

        Product product = productRepository.findById(newProductionDto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + newProductionDto.getProductId()));

        production.setProduct(product);
        production.setDateOfProduction(newProductionDto.getDateOfProduction());
        production.setQuantity(newProductionDto.getQuantity());
        production.setUnitPrice(newProductionDto.getUnitPrice());
        production.setAmount(newProductionDto.getUnitPrice().multiply(BigDecimal.valueOf(newProductionDto.getQuantity())));

        Map<Long, ProductionItem> existingItems = production.getProductionItems().stream()
                .collect(Collectors.toMap(item -> item.getProduct().getId(), item -> item));

        List<Long> newProductIds = newProductionDto.getProductionItems().stream()
                .map(item -> item.getProductId())
                .toList();

        List<ProductionItem> updatedItems = new ArrayList<>();

        for (NewProductionItemDto itemDto : newProductionDto.getProductionItems()) {
            Long productId = itemDto.getProductId();
            ProductionItem productionItem = existingItems.get(productId);

            if (productionItem == null) {
                productionItem = new ProductionItem();
                productionItem.setProduction(production);
                productionItem.setProduct(productRepository.findById(productId)
                        .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId)));
            }

            productionItem.setQuantity(itemDto.getQuantity());
            productionItem.setUnitPrice(itemDto.getUnitPrice());
            productionItem.setTotalPrice(itemDto.getUnitPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));

            updatedItems.add(productionItem);
        }

        // Удаляем старые элементы, которых нет в новом списке (НЕ заменяем коллекцию)
        production.getProductionItems().removeIf(item -> !newProductIds.contains(item.getProduct().getId()));

        // Добавляем/обновляем существующие элементы
        for (ProductionItem updatedItem : updatedItems) {
            if (!production.getProductionItems().contains(updatedItem)) {
                production.getProductionItems().add(updatedItem);
            }
        }

        Production updatedProduction = productionRepository.save(production);
        return modelMapper.map(updatedProduction, ProductionDto.class);
    }

    @Override
    @Transactional
    public void deleteProduction(Long id) {
        Production production = productionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Production not found with ID: " + id));
        productionRepository.delete(production);
    }
}
