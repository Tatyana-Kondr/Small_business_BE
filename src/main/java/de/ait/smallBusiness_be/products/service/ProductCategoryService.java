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
    ProductCategoryDto getProductCategoryById(Integer id);
    ProductCategoryDto deleteProductCategoryById(Integer id);
    ProductCategoryDto updateProductCategory(Integer id, NewProductCategoryDto newProductCategoryDto);
    List<ProductCategoryDto> findAllProductCategories();

}
