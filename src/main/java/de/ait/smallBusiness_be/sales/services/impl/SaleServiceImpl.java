package de.ait.smallBusiness_be.sales.services.impl;

import de.ait.smallBusiness_be.customers.dao.CustomerRepository;
import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.products.dao.ProductRepository;
import de.ait.smallBusiness_be.sales.dao.SaleRepository;
import de.ait.smallBusiness_be.sales.dto.NewSaleDto;
import de.ait.smallBusiness_be.sales.dto.SaleDto;
import de.ait.smallBusiness_be.sales.models.Sale;
import de.ait.smallBusiness_be.sales.services.SaleService;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.mapper.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 2/5/2025
 * Small_business_BE
 *
 * @author Chechkina (AIT TR)
 */
@RequiredArgsConstructor
@Service
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public SaleDto createSale(NewSaleDto newSale) {

        Customer customer = customerRepository
                .findById(newSale.customerId())
                .orElseThrow(()-> new RestApiException(ErrorDescription.CUSTOMER_NOT_FOUND));

        return null;
    }

    @Override
    public Page<SaleDto> getAllSales(Pageable pageable) {
        return null;
    }

    @Override
    public SaleDto getSaleById(Long id) {

        Sale sale = getSaleOrThrow(id);
        return modelMapper.map(sale, SaleDto.class);
    }

    @Override
    public SaleDto updateSale(Long saleId, NewSaleDto updateSale) {
        return null;
    }

    @Override
    public void deleteSale(Long saleId) {

    }

    public Sale getSaleOrThrow(Long id) {

        return saleRepository
                .findById(id)
                .orElseThrow(() -> new RestApiException(ErrorDescription.SALE_NOT_FOUND, HttpStatus.NOT_FOUND));
    }
}
