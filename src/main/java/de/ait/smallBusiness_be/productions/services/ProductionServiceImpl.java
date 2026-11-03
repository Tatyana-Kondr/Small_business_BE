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
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


/**
 * 13.02.2025
 * SmB_be
 *
 * @author Kondratyeva (AIT TR)
 */

@Service
@RequiredArgsConstructor
public class ProductionServiceImpl implements ProductionService {

    private final ProductionRepository productionRepository;
    private final ProductRepository productRepository;
    private final WarehouseService warehouseService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public ProductionDto createProduction(NewProductionDto newProductionDto) {
        Product product = getProductOrThrow(newProductionDto.getProductId());

        Production production = modelMapper.map(newProductionDto, Production.class);
        production.setProductionItems(new ArrayList<>());
        production.setProduct(product);
        production.setType(TypeOfOperation.PRODUKTION);

        production.setAmount(calculateAmount(newProductionDto.getUnitPrice(), newProductionDto.getQuantity()));

        BigDecimal totalPriceItems = BigDecimal.ZERO;

        for (NewProductionItemDto dto : newProductionDto.getProductionItems()) {
            ProductionItem item = createProductionItem(dto, production);
            production.getProductionItems().add(item);
            totalPriceItems = totalPriceItems.add(item.getTotalPrice());
        }

        validateProductionAmount(production.getAmount(), totalPriceItems);

        Production savedProduction = productionRepository.save(production);

        recordWarehouseOperations(savedProduction);

        return modelMapper.map(savedProduction, ProductionDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductionDto> getAllProductions(Pageable pageable) {
        Page<Production> productions = productionRepository.findAllWithSorting(pageable);
        initializeProductionItems(productions);
        return productions.map(production -> modelMapper.map(production, ProductionDto.class));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductionDto getProductionById(Long id) {
        Production production = getProductionOrThrow(id);
        return modelMapper.map(production, ProductionDto.class);
    }

    @Override
    @Transactional
    public ProductionDto updateProduction(Long id, NewProductionDto newProductionDto) {
        Production production = getProductionOrThrow(id);
        Product product = getProductOrThrow(newProductionDto.getProductId());

        // Обновляем базовые поля
        production.setProduct(product);
        production.setDateOfProduction(newProductionDto.getDateOfProduction());
        production.setType(TypeOfOperation.PRODUKTION);
        production.setQuantity(newProductionDto.getQuantity());
        production.setUnitPrice(newProductionDto.getUnitPrice());
        production.setAmount(calculateAmount(newProductionDto.getUnitPrice(), newProductionDto.getQuantity()));
        // Удаляем все старые позиции
        production.getProductionItems().clear();

        BigDecimal totalPriceItems = BigDecimal.ZERO;

        for (NewProductionItemDto dto : newProductionDto.getProductionItems()) {
            ProductionItem item = createProductionItem(dto, production);
            production.getProductionItems().add(item);
            totalPriceItems = totalPriceItems.add(item.getTotalPrice());
        }

        validateProductionAmount(production.getAmount(), totalPriceItems);

        Production updatedProduction = productionRepository.save(production);

        // Откатываем старые движения документа
        warehouseService.rollbackDocument(
                updatedProduction.getId(),
                List.of(TypeOfOperation.PRODUKTION, TypeOfOperation.PRODUKTIONSMATERIAL)
        );
        recordWarehouseOperations(updatedProduction);

        return modelMapper.map(updatedProduction, ProductionDto.class);
    }

    @Override
    @Transactional
    public void deleteProduction(Long id) {
        Production production = getProductionOrThrow(id);
        warehouseService.rollbackDocument(
                production.getId(),
                List.of(TypeOfOperation.PRODUKTION, TypeOfOperation.PRODUKTIONSMATERIAL));
        productionRepository.delete(production);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductionDto> searchProduction(Pageable pageable, String query) {
        Page<Production> productions = productionRepository.searchProduction(pageable, query);
        initializeProductionItems(productions);
        return productions.map(production -> modelMapper.map(production, ProductionDto.class));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductionDto> getAllProductionsByFilter(Pageable pageable, LocalDate startDate, LocalDate endDate, String searchQuery) {
        Page<Production> productions = productionRepository.getAllProductionsByFilter(pageable, startDate, endDate, searchQuery);
        initializeProductionItems(productions);
        return productions.map(production -> modelMapper.map(production, ProductionDto.class));
    }

    private Product getProductOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.PRODUCT_NOT_FOUND.getDescription()));
    }

    private Production getProductionOrThrow(Long id) {
        return productionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.PRODUCTION_NOT_FOUND.getDescription()));
    }

    private BigDecimal calculateAmount(BigDecimal unitPrice, BigDecimal quantity) {
        return unitPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
    }

    private ProductionItem createProductionItem(NewProductionItemDto dto, Production production) {
        Product product = getProductOrThrow(dto.getProductId());

        ProductionItem item = modelMapper.map(dto, ProductionItem.class);

        item.setProduction(production);
        item.setProduct(product);
        item.setType(TypeOfOperation.PRODUKTIONSMATERIAL);
        item.setTotalPrice(calculateAmount(dto.getUnitPrice(), dto.getQuantity()));
        return item;
    }

    private void initializeProductionItems(Page<Production> productions) {
        productions.forEach(production -> {
            Hibernate.initialize(production.getProductionItems());
            production.getProductionItems().forEach(item -> Hibernate.initialize(item.getProduct()));
        });
    }

    private void validateProductionAmount(BigDecimal productionAmount, BigDecimal materialsAmount) {
        if (productionAmount.compareTo(materialsAmount) <= 0) {
            throw new RestApiException(ErrorDescription.PRODUCTION_AMOUNT, HttpStatus.CONFLICT);
        }
    }

    private void recordWarehouseOperations(Production production) {
        production.getProductionItems().forEach(item -> {
            warehouseService.recordOperation(
                    item.getProduct(),
                    TypeOfOperation.PRODUKTIONSMATERIAL,
                    item.getQuantity(),
                    production.getId(),
                    null,
                    production.getDateOfProduction()
            );
        });

        warehouseService.recordOperation(
                production.getProduct(),
                TypeOfOperation.PRODUKTION,
                production.getQuantity(),
                production.getId(),
                null,
                production.getDateOfProduction()
        );
    }
}
