package de.ait.smallBusiness_be.productions.services;

import de.ait.smallBusiness_be.productions.dao.ProductionItemRepository;
import de.ait.smallBusiness_be.productions.dao.ProductionRepository;
import de.ait.smallBusiness_be.productions.dto.NewProductionItemDto;
import de.ait.smallBusiness_be.productions.dto.ProductionItemDto;
import de.ait.smallBusiness_be.productions.model.Production;
import de.ait.smallBusiness_be.productions.model.ProductionItem;
import de.ait.smallBusiness_be.products.dao.ProductRepository;
import de.ait.smallBusiness_be.products.model.Product;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 13.02.2025
 * SmB_be
 *
 * @author Kondratyeva (AIT TR)
 */

@Service
@RequiredArgsConstructor
public class ProductionItemServiceImpl implements ProductionItemService {

    private final ProductionRepository productionRepository;
    private final ProductionItemRepository productionItemRepository;
    private final ProductRepository productRepository;
    private final ModelMapper  modelMapper;


    @Override
    @Transactional
    public ProductionItemDto createProductionItem(NewProductionItemDto newProductionItemDto, Long productionId) {
        if(productionId == null){
            throw new IllegalArgumentException("ProductionID must not be null");
        }
        Production production = productionRepository.findById(productionId)
                .orElseThrow(() -> new IllegalArgumentException("Production not found with ID: " + productionId));
        Product product = productRepository.findById(newProductionItemDto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + newProductionItemDto.getProductId()));

        ProductionItem productionItem = modelMapper.map(newProductionItemDto, ProductionItem.class);
        productionItem.setProduction(production);
        productionItem.setProduct(product);
        BigDecimal totalPrice = productionItem.getUnitPrice().multiply(productionItem.getQuantity());
        productionItem.setTotalPrice(totalPrice);

        ProductionItem savedProductionItem = productionItemRepository.save(productionItem);
        return modelMapper.map(savedProductionItem, ProductionItemDto.class);
    }

    @Override
    public ProductionItemDto getProductionItemById(Long id) {
        ProductionItem productionItem = productionItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ProductionItem not found with ID: " + id));
        return modelMapper.map(productionItem, ProductionItemDto.class);
    }

    @Override
    @Transactional
    public ProductionItemDto updateProductionItem(Long id, NewProductionItemDto newProductionItemDto) {
        ProductionItem productionItem = productionItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ProductionItem not found with ID: " + id));
        modelMapper.map(newProductionItemDto, productionItem);
        BigDecimal totalPrice = productionItem.getUnitPrice().multiply(productionItem.getQuantity());
        productionItem.setTotalPrice(totalPrice);
        ProductionItem updatedProductionItem = productionItemRepository.save(productionItem);
        return modelMapper.map(updatedProductionItem, ProductionItemDto.class);
    }

    @Override
    @Transactional
    public void deleteProductionItem(Long id) {
        ProductionItem productionItem = productionItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ProductionItem not found with ID: " + id));
        Production production = productionItem.getProduction();
        Hibernate.initialize(production.getProductionItems()); // Загружаем коллекцию перед удалением
        production.getProductionItems().remove(productionItem);// Удаляем Item из коллекции Production
        productionItemRepository.delete(productionItem);// Удаляем ProductionItem из базы
        productionRepository.save(production);
    }
}
