package de.ait.smallBusiness_be.products.service;


import de.ait.smallBusiness_be.products.dto.NewProductCategoryDto;
import de.ait.smallBusiness_be.products.dto.ProductCategoryDto;

import java.util.List;

/**
 * SmallBusiness_BE
 * 24.10.2024
 *
 * @author Kondratyeva
 */

public interface ProductCategoryService {
    ProductCategoryDto addProductCategory(NewProductCategoryDto newProductCategoryDto);
    ProductCategoryDto getProductCategoryById(int id);
    ProductCategoryDto deleteProductCategoryById(int id);
    ProductCategoryDto updateProductCategory(int id, NewProductCategoryDto newProductCategoryDto);
    List<ProductCategoryDto> findAllProductCategories();

}
