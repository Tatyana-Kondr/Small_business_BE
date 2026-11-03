package de.ait.smallBusiness_be.products.service;


import de.ait.smallBusiness_be.products.dto.*;
import de.ait.smallBusiness_be.products.model.Product;
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
    ProductDto updateProductArticle(Long id, UpdateProductArticleDto dto);
    Page<ProductDto> findProducts(String searchTerm, Pageable pageable);
    Page<ProductDto> findProductsByCategoryId(Long categoryId, String searchTerm, Pageable pageable);
    Product getProductOrThrow(Long id);
    List<ProductPickDto> pick(String searchTerm, Long categoryId, Integer limit);
    List<ProductPickDto> findSimilarProducts(String name, String vendorArticle);
    List<ProductPickDto> findSimilarProductsForUpdate(Long productId, String name, String article, String vendorArticle);
}
