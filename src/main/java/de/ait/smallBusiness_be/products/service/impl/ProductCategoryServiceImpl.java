package de.ait.smallBusiness_be.products.service.impl;


import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.products.dao.ProductCategoryRepository;
import de.ait.smallBusiness_be.products.dto.NewProductCategoryDto;
import de.ait.smallBusiness_be.products.dto.ProductCategoryDto;
import de.ait.smallBusiness_be.products.model.ProductCategory;
import de.ait.smallBusiness_be.products.service.ProductCategoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SmallBusiness_BE
 * 31.10.2024
 *
 * @author Kondratyeva
 */

@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {

    final ProductCategoryRepository productCategoryRepository;
    final ModelMapper modelMapper;

    @Override
    public ProductCategoryDto addProductCategory(NewProductCategoryDto newProductCategoryDto) {

        if (productCategoryRepository.findByNameIgnoreCase(newProductCategoryDto.getName()).isPresent()) {
            throw new RestApiException(ErrorDescription.CATEGORY_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }
            ProductCategory productCategory = modelMapper.map(newProductCategoryDto, ProductCategory.class);
            ProductCategory savedCategory = productCategoryRepository.save(productCategory);
            return modelMapper.map(savedCategory, ProductCategoryDto.class);
    }

    @Override
    public ProductCategoryDto getProductCategoryById(int id) {
        ProductCategory productCategory = productCategoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product category not found"));

        return modelMapper.map(productCategory, ProductCategoryDto.class);
    }

    @Override
    public ProductCategoryDto deleteProductCategoryById(int id) {
        ProductCategory productCategory = productCategoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product category not found"));

        productCategoryRepository.delete(productCategory);

        return modelMapper.map(productCategory, ProductCategoryDto.class);
    }

    @Override
    public ProductCategoryDto updateProductCategory(int id, NewProductCategoryDto newProductCategoryDto) {
        ProductCategory productCategory = productCategoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product category not found"));

        // Обновляем поля
        productCategory.setName(newProductCategoryDto.getName());

        // Сохраняем обновленную категорию
        ProductCategory updatedCategory = productCategoryRepository.save(productCategory);

        return modelMapper.map(updatedCategory, ProductCategoryDto.class);
    }

    @Override
    public List<ProductCategoryDto> findAllProductCategories() {
        List<ProductCategory> categories = productCategoryRepository.findAll();

        return categories.stream()
                .map(category -> modelMapper.map(category, ProductCategoryDto.class))
                .collect(Collectors.toList());
    }
}
