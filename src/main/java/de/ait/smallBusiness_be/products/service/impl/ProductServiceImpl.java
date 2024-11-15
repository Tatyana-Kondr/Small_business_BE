package de.ait.smallBusiness_be.products.service.impl;

import de.ait.smallBusiness_be.products.dao.ProductRepository;
import de.ait.smallBusiness_be.products.dto.NewProductDto;
import de.ait.smallBusiness_be.products.dto.ProductDto;
import de.ait.smallBusiness_be.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 11/15/2024
 * Small_business_BE
 *
 * @author Chechkina (AIT TR)
 */

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    final ProductRepository productRepository;
    final ModelMapper modelMapper;

    @Override
    public ProductDto addProduct(NewProductDto newProductDto) {
        return null;
    }

    @Override
    public ProductDto getProductById(Long id) {
        return null;
    }

    @Override
    public ProductDto deleteProductById(Long id) {
        return null;
    }

    @Override
    public ProductDto updateProduct(Long id, NewProductDto newProductDto) {
        return null;
    }

    @Override
    public List<ProductDto> findAllProducts() {
        return null;
    }
}
