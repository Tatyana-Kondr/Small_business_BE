package de.ait.smallBusiness_be.productions.services;

import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.productions.dao.ProductionRepository;
import de.ait.smallBusiness_be.productions.dto.NewProductionDto;
import de.ait.smallBusiness_be.productions.dto.NewProductionItemDto;
import de.ait.smallBusiness_be.productions.dto.ProductionDto;
import de.ait.smallBusiness_be.productions.model.Production;
import de.ait.smallBusiness_be.productions.model.ProductionItem;
import de.ait.smallBusiness_be.products.dao.ProductRepository;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.purchases.model.TypeOfOperation;
import de.ait.smallBusiness_be.warehouse.services.WarehouseService;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


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
    private final ProductRepository productRepository;
    private final WarehouseService warehouseService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public ProductionDto createProduction(NewProductionDto newProductionDto) {
        Product product = productRepository.findById(newProductionDto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + newProductionDto.getProductId()));
        Production production = modelMapper.map(newProductionDto, Production.class);
        production.setProduct(product);

        BigDecimal amount = production.getUnitPrice()
                .multiply(production.getQuantity())
                .setScale(2, RoundingMode.HALF_UP);

        production.setAmount(amount);

        AtomicReference<BigDecimal> totalPriceItems = new AtomicReference<>(BigDecimal.ZERO);

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
                        BigDecimal totalPrice = productionItem.getUnitPrice()
                                .multiply(productionItem.getQuantity())
                                .setScale(2, RoundingMode.HALF_UP);
                        productionItem.setTotalPrice(totalPrice);

                        totalPriceItems.updateAndGet(value -> value.add(productionItem.getTotalPrice()));

                        return productionItem;
                    })
                    .toList();

            production.setProductionItems(productionItems);
        }
        if (production.getAmount().compareTo(totalPriceItems.get()) <= 0) {
            throw new RestApiException(ErrorDescription.PRODUCTION_AMOUNT, HttpStatus.CONFLICT);
        }
        Production savedProduction = productionRepository.save(production);

        // Обновляем склад
        savedProduction.getProductionItems().forEach(item -> {
            warehouseService.recordOperation(
                    item.getProduct(),
                    TypeOfOperation.PRODUKTIONSMATERIAL,
                    item.getQuantity(),
                    savedProduction.getId(),
                    null,
                    savedProduction.getDateOfProduction()
            );
        });
            warehouseService.recordOperation(
                    savedProduction.getProduct(),
                    TypeOfOperation.PRODUKTION,
                    savedProduction.getQuantity(),
                    savedProduction.getId(),
                    null,
                    savedProduction.getDateOfProduction()
            );


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

        return productions.map(production -> {
            // Инициализируем ленивую коллекцию productionItems
            Hibernate.initialize(production.getProductionItems());
            // Инициализируем вложенные продукты для productionItems
            production.getProductionItems().forEach(item -> Hibernate.initialize(item.getProduct()));

            return modelMapper.map(production, ProductionDto.class);
        });
    }

    @Override
    @Transactional
    public ProductionDto getProductionById(Long id) {
        Production production = productionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Production not found with ID: " + id));
        Hibernate.initialize(production.getProductionItems());
        production.getProductionItems().forEach(item -> Hibernate.initialize(item.getProduct()));
        return modelMapper.map(production, ProductionDto.class);
    }

    @Override
    @Transactional
    public ProductionDto updateProduction(Long id, NewProductionDto newProductionDto) {
        Production production = productionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Production not found"));

        Product product = productRepository.findById(newProductionDto.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + newProductionDto.getProductId()));

        // Обновляем базовые поля
        production.setProduct(product);
        production.setDateOfProduction(newProductionDto.getDateOfProduction());
        try {
            production.setType(TypeOfOperation.valueOf(newProductionDto.getType().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid type of operation: " + newProductionDto.getType());
        }
        production.setQuantity(newProductionDto.getQuantity());
        production.setUnitPrice(newProductionDto.getUnitPrice());
        production.setAmount(newProductionDto.getUnitPrice().multiply(newProductionDto.getQuantity()).setScale(2, RoundingMode.HALF_UP));

        // Удаляем все старые позиции
        production.getProductionItems().clear();

        // Пересчет сумм
        AtomicReference<BigDecimal> materialsTotal = new AtomicReference<>(BigDecimal.ZERO);
        List<ProductionItem> newItems = new ArrayList<>();

        for (NewProductionItemDto itemDto : newProductionDto.getProductionItems()) {
            Product itemProduct = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found: " + itemDto.getProductId()));

            ProductionItem item = new ProductionItem();
            item.setProduction(production);
            item.setProduct(itemProduct);
            try {
                item.setType(TypeOfOperation.valueOf(itemDto.getType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid type for ProductionItem: " + itemDto.getType());
            }
            item.setQuantity(itemDto.getQuantity());
            item.setUnitPrice(itemDto.getUnitPrice());
            item.setTotalPrice(itemDto.getUnitPrice().multiply(itemDto.getQuantity()).setScale(2, RoundingMode.HALF_UP));

            newItems.add(item);
            materialsTotal.updateAndGet(v -> v.add(item.getTotalPrice()));
        }

        if (newItems.isEmpty()) {
            throw new RestApiException(ErrorDescription.NO_PRODUCT_IN_PRODUCTION);
        }

        // Добавляем новые позиции
        production.getProductionItems().addAll(newItems);

        // Проверка: сумма главного продукта должна быть больше суммы материалов
        if (production.getAmount().compareTo(materialsTotal.get()) <= 0) {
            throw new RestApiException(ErrorDescription.PRODUCTION_AMOUNT, HttpStatus.CONFLICT);
        }

        Production updatedProduction = productionRepository.save(production);

        // Синхронизируем документ со складом
        warehouseService.syncDocument(
                TypeOfOperation.PRODUKTIONSMATERIAL,
                updatedProduction.getId(),
                null,
                updatedProduction.getDateOfProduction(),
                updatedProduction.getProductionItems()
        );
        warehouseService.syncDocument(
                TypeOfOperation.PRODUKTION,
                updatedProduction.getId(),
                null,
                updatedProduction.getDateOfProduction(),
                List.of(production)
        );

        return modelMapper.map(updatedProduction, ProductionDto.class);
    }

    @Override
    @Transactional
    public void deleteProduction(Long id) {
        Production production = productionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Production not found with ID: " + id));
        warehouseService.rollbackDocument(production.getId());
        productionRepository.delete(production);
    }

    @Override
    @Transactional
    public Page<ProductionDto> searchProduction(Pageable pageable, String query) {
        Page<Production> productions = productionRepository.searchProduction(pageable, query);

        // Инициализация ленивых коллекций и вложенных продуктов
        productions.forEach(production -> {
            Hibernate.initialize(production.getProductionItems());
            production.getProductionItems().forEach(item -> Hibernate.initialize(item.getProduct()));
        });

        return productions.map(production -> modelMapper.map(production, ProductionDto.class));
    }

    @Override
    @Transactional
    public Page<ProductionDto> getAllProductionsByFilter(Pageable pageable, LocalDate startDate, LocalDate endDate, String searchQuery) {
        Page<Production> productions = productionRepository.getAllProductionsByFilter(pageable, startDate, endDate, searchQuery);

        // Инициализация ленивых коллекций и вложенных продуктов
        productions.forEach(production -> {
            Hibernate.initialize(production.getProductionItems());
            production.getProductionItems().forEach(item -> Hibernate.initialize(item.getProduct()));
        });

        return productions.map(production -> modelMapper.map(production, ProductionDto.class));
    }

}
