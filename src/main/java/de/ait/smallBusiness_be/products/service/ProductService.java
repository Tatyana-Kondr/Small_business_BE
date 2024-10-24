package de.ait.smallBusiness_be.products.service;


import de.ait.smallBusiness_be.products.dto.NewProductDto;
import de.ait.smallBusiness_be.products.dto.ProductDto;

import java.util.List;

/**
 * SmallBusiness_BE
 * 24.10.2024
 *
 * @author Kondratyeva
 */

public interface ProductService {

    ProductDto addProduct(NewProductDto newProductDto);
    ProductDto getProductById(Long id);
    ProductDto deleteProductById(Long id);
    ProductDto updateProduct(Long id, NewProductDto newProductDto);
    List<ProductDto> findAllProducts();
}
