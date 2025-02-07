package de.ait.smallBusiness_be.products.service;


import de.ait.smallBusiness_be.products.dto.NewProductDto;
import de.ait.smallBusiness_be.products.dto.UpdateProductDto;
import de.ait.smallBusiness_be.products.dto.ProductDto;
import de.ait.smallBusiness_be.products.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    Product getProductOrThrow(Long id);
}
