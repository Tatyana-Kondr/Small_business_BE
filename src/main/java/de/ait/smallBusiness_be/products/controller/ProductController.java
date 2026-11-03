package de.ait.smallBusiness_be.products.controller;

import de.ait.smallBusiness_be.products.controller.api.ProductsApi;
import de.ait.smallBusiness_be.products.dto.*;
import de.ait.smallBusiness_be.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SmallBusiness_BE
 * 24.10.2024
 *
 * @author Kondratyeva
 */

@RestController
@RequiredArgsConstructor
public class ProductController implements ProductsApi {

    private final ProductService productService;

    @Override
    public ProductDto createProduct(NewProductDto newProductDto) {
        return productService.addProduct(newProductDto);
    }

    @Override
    public ProductDto getProductById(Long id) {
            return productService.getProductById(id);
    }

    @Override
    public ProductDto updateProductById(Long id, UpdateProductDto updateProductDto) {
            return productService.updateProduct(id, updateProductDto);
    }

    @Override
    public ProductDto updateProductArticle(Long id, UpdateProductArticleDto dto) {
        return productService.updateProductArticle(id, dto);
    }

    @Override
    public void removeProductById(Long id) {
        productService.deleteProductById(id);
    }

    @Override
    public List<ProductPickDto> pickProducts(String search, Long categoryId, Integer limit) {
        return productService.pick(search, categoryId, limit);
    }

    @Override
    public Page<ProductDto> getProductsPaged(String search, Pageable pageable) {
        return productService.findProducts(search, pageable);
    }

    @Override
    public Page<ProductDto> getProductsByCategoryPaged(Long categoryId, String search, Pageable pageable) {
        return productService.findProductsByCategoryId(categoryId, search, pageable);
    }

    @Override
    public List<ProductPickDto> findSimilarProducts(
            String name,
            String vendorArticle
    ) {
        return productService.findSimilarProducts(name, vendorArticle);
    }

    @Override
    public List<ProductPickDto> findSimilarProductsForUpdate(Long productId, String name, String article, String vendorArticle) {
        return productService.findSimilarProductsForUpdate(productId, name, article, vendorArticle);
    }

}
