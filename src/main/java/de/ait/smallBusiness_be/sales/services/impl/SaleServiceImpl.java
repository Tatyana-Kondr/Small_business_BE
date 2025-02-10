package de.ait.smallBusiness_be.sales.services.impl;

import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.customers.services.CustomerService;
import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.products.service.ProductService;
import de.ait.smallBusiness_be.sales.dao.SaleRepository;
import de.ait.smallBusiness_be.sales.dto.NewSaleDto;
import de.ait.smallBusiness_be.sales.dto.SaleDto;
import de.ait.smallBusiness_be.sales.models.Sale;
import de.ait.smallBusiness_be.sales.models.SaleItem;
import de.ait.smallBusiness_be.sales.models.ShippingDimensions;
import de.ait.smallBusiness_be.sales.services.SaleService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    private final CustomerService customerService;
    private final ProductService productService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public SaleDto createSale(NewSaleDto newSale) {

        Customer customer = customerService.getCustomerOrThrow(newSale.customerId());

        Sale sale = modelMapper.map(newSale, Sale.class);
        sale.setCustomer(customer);
        sale.setShippingDimensions(newSale.shippingDimensions() != null ?
                modelMapper.map(newSale.shippingDimensions(), ShippingDimensions.class) :
                null);

        if (newSale.salesItems()!=null && !newSale.salesItems().isEmpty()) {

            List<SaleItem> saleItems = newSale.salesItems().stream()
                    .map(newSaleItemDto -> {
                        Product product = productService.getProductOrThrow(newSaleItemDto.productId());
                        SaleItem saleItem = modelMapper.map(newSaleItemDto, SaleItem.class);
                        saleItem.setProduct(product);
                        saleItem.setSale(sale);
                        return saleItem;
                    }).toList();
            sale.setSalesItems(saleItems);
        }

        Sale savedSale = saleRepository.save(sale);

        return modelMapper.map(savedSale, SaleDto.class);
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

    @Override
    public boolean checkIfSaleExistsById(Long saleId) {

        return saleRepository.existsById(saleId);
    }
}
