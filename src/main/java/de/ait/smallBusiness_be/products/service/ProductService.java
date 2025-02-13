package de.ait.smallBusiness_be.products.service;


import de.ait.smallBusiness_be.products.dto.NewProductDto;
import de.ait.smallBusiness_be.products.dto.UpdateProductDto;
import de.ait.smallBusiness_be.products.dto.ProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    void deleteProductById(Long id);
    ProductDto updateProduct(Long id, UpdateProductDto updateProductDto);
    Page<ProductDto> findAllProducts(Pageable pageable);
    Page<ProductDto> findProductsByCategoryId(int categoryId, Pageable pageable);
    ProductDto findProductByArticle(String article);
    List<ProductDto> findProductsByVendorArticle(String vendorArticle);
    List<ProductDto> findProductsByName(String name);
    List<ProductDto> searchProducts(String searchTerm);
}
