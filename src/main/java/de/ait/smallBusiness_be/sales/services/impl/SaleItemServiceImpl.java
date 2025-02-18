package de.ait.smallBusiness_be.sales.services.impl;

import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.products.service.ProductService;
import de.ait.smallBusiness_be.sales.dao.SaleItemRepository;
import de.ait.smallBusiness_be.sales.dto.NewSaleItemDto;
import de.ait.smallBusiness_be.sales.dto.SaleItemDto;
import de.ait.smallBusiness_be.sales.models.Sale;
import de.ait.smallBusiness_be.sales.models.SaleItem;
import de.ait.smallBusiness_be.sales.services.SaleItemService;
import de.ait.smallBusiness_be.sales.services.SaleService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    @Override
    @Transactional
    public SaleItemDto createSaleItem(Long saleId, NewSaleItemDto newSaleItem) {

        Sale sale = saleService.getSaleOrThrow(saleId);

        Product product = productService.getProductOrThrow(newSaleItem.productId());

        SaleItem saleItem = modelMapper.map(newSaleItem, SaleItem.class);
        saleItem.setProduct(product);
        saleItem.setSale(sale);

        SaleItem savedItem = saleItemRepository.save(saleItem);

        return modelMapper.map(savedItem, SaleItemDto.class);
    }

    @Override
    public List<SaleItemDto> getAllSaleItemsBySaleId(Long saleId) {

        if (!saleService.checkIfSaleExistsById(saleId)) {
            throw new IllegalArgumentException("Sale with ID: " + saleId + " does not exist");
        }
        return saleItemRepository.findAllBySaleIdOrderByPosition(saleId)
                .stream()
                .map(item -> modelMapper.map(item, SaleItemDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public SaleItemDto getSaleItemById(Long saleId, Long saleItemId) {
        return null;
    }

    @Override
    public SaleItemDto updateSaleItem(Long saleId, Long saleItemId, NewSaleItemDto updateSaleItem) {
        return null;
    }

    @Override
    public void deleteSaleItem(Long saleId, Long saleItemId) {
    }
}
