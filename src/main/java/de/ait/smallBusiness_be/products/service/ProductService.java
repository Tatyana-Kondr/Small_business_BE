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
    ProductDto getProductById(int id);
    void deleteProductById(int id);
    ProductDto updateProduct(int id, NewProductDto newProductDto);
    List<ProductDto> findAllProducts();
}
