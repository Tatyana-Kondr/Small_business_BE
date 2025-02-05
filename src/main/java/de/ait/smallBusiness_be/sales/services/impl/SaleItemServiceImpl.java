package de.ait.smallBusiness_be.sales.services.impl;

import de.ait.smallBusiness_be.customers.dao.CustomerRepository;
import de.ait.smallBusiness_be.products.dao.ProductRepository;
import de.ait.smallBusiness_be.sales.dao.SaleItemRepository;
import de.ait.smallBusiness_be.sales.dao.SaleRepository;
import de.ait.smallBusiness_be.sales.dto.NewSaleItemDto;
import de.ait.smallBusiness_be.sales.dto.SaleItemDto;
import de.ait.smallBusiness_be.sales.services.SaleItemService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

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
    private final SaleRepository saleRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Override
    public SaleItemDto createSaleItem(Long saleId, NewSaleItemDto newSaleItem) {
        return null;
    }

    @Override
    public List<SaleItemDto> getAllSales(Long saleId) {
        return List.of();
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
