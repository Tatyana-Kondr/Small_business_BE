package de.ait.smallBusiness_be.products.controller;

import de.ait.smallBusiness_be.products.controller.api.ProductCategoryApi;
import de.ait.smallBusiness_be.products.dto.NewProductCategoryDto;
import de.ait.smallBusiness_be.products.dto.ProductCategoryDto;
import de.ait.smallBusiness_be.products.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
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
public class ProductCategoryController implements ProductCategoryApi {

    final ProductCategoryService productCategoryService;

    @Override
    public ProductCategoryDto createProductCategory(NewProductCategoryDto newCategory) {
        return productCategoryService.addProductCategory(newCategory);
    }

    @Override
    public List<ProductCategoryDto> getAllProductCategories() {
        return productCategoryService.findAllProductCategories();
    }

    @Override
    public ProductCategoryDto getProductCategoryById(int id) {
        return productCategoryService.getProductCategoryById(id);
    }

    @Override
    public ProductCategoryDto updateProductCategoryById(int id, NewProductCategoryDto updatedCategory) {
        return productCategoryService.updateProductCategory(id, updatedCategory);
    }

    @Override
    public void removeProductCategoryById(int id) {
        productCategoryService.deleteProductCategoryById(id);
    }
}
