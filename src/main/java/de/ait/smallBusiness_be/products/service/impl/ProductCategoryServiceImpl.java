package de.ait.smallBusiness_be.products.service.impl;


import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.FieldValidationException;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.products.dao.ProductCategoryRepository;
import de.ait.smallBusiness_be.products.dao.ProductRepository;
import de.ait.smallBusiness_be.products.dto.NewProductCategoryDto;
import de.ait.smallBusiness_be.products.dto.ProductCategoryDto;
import de.ait.smallBusiness_be.products.model.ProductCategory;
import de.ait.smallBusiness_be.products.service.ProductCategoryService;
import de.ait.smallBusiness_be.validation.dto.ValidationErrorDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    private final ProductRepository productRepository;
    final ModelMapper modelMapper;

    @Override
    @Transactional
    public ProductCategoryDto addProductCategory(NewProductCategoryDto newProductCategoryDto) {
        String nameToUpperCase = newProductCategoryDto.getName()
                .trim()
                .toUpperCase(Locale.ROOT);

        String artNameToUpperCase = newProductCategoryDto.getArtName()
                .trim()
                .toUpperCase(Locale.ROOT);

        List<ValidationErrorDto> errors = new ArrayList<>();

        if (productCategoryRepository.existsByNameIgnoreCase(nameToUpperCase)) {
            errors.add(ValidationErrorDto.builder()
                    .field("name")
                    .message(ErrorDescription.CATEGORY_ALREADY_EXISTS.getDescription())
                    .build());
        }

        if (productCategoryRepository.existsByArtNameIgnoreCase(artNameToUpperCase)) {
            errors.add(ValidationErrorDto.builder()
                    .field("artName")
                    .message(ErrorDescription.CATEGORY_ARTNAME_ALREADY_EXISTS.getDescription())
                    .build());
        }

        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }

        ProductCategory productCategory = new ProductCategory();
        productCategory.setName(nameToUpperCase);
        productCategory.setArtName(artNameToUpperCase);

        ProductCategory savedCategory = productCategoryRepository.save(productCategory);

        return modelMapper.map(savedCategory, ProductCategoryDto.class);
    }

    @Override
    public ProductCategoryDto getProductCategoryById(Long id) {
        ProductCategory productCategory = getProductCategoryOrThrow(id);

        return modelMapper.map(productCategory, ProductCategoryDto.class);
    }

    @Override
    @Transactional
    public void deleteProductCategoryById(Long id) {
        ProductCategory category = getProductCategoryOrThrow(id);

        if (productRepository.existsByProductCategory_Id(id)) {
            throw new RestApiException(
                    ErrorDescription.CATEGORY_DELETE_FAILED,
                    HttpStatus.CONFLICT
            );
        }

        try {
            productCategoryRepository.delete(category);
            productCategoryRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new RestApiException(
                    ErrorDescription.CATEGORY_DELETE_FAILED,
                    HttpStatus.CONFLICT
            );
        }
    }

    @Override
    @Transactional
    public ProductCategoryDto updateProductCategory(
            Long id,
            NewProductCategoryDto newProductCategoryDto
    ) {
        ProductCategory productCategory = getProductCategoryOrThrow(id);

        String nameToUpperCase = newProductCategoryDto.getName()
                .trim()
                .toUpperCase(Locale.ROOT);

        String artNameToUpperCase = newProductCategoryDto.getArtName()
                .trim()
                .toUpperCase(Locale.ROOT);

        List<ValidationErrorDto> errors = new ArrayList<>();

        if (productCategoryRepository.existsByNameIgnoreCaseAndIdNot(nameToUpperCase, id)) {
            errors.add(ValidationErrorDto.builder()
                    .field("name")
                    .message(ErrorDescription.CATEGORY_ALREADY_EXISTS.getDescription())
                    .build());
        }

        if (productCategoryRepository.existsByArtNameIgnoreCaseAndIdNot(artNameToUpperCase, id)) {
            errors.add(ValidationErrorDto.builder()
                    .field("artName")
                    .message(ErrorDescription.CATEGORY_ARTNAME_ALREADY_EXISTS.getDescription())
                    .build());
        }

        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }

        productCategory.setName(nameToUpperCase);
        productCategory.setArtName(artNameToUpperCase);

        ProductCategory updatedCategory = productCategoryRepository.save(productCategory);

        return modelMapper.map(updatedCategory, ProductCategoryDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductCategoryDto> findAllProductCategories() {
        return productCategoryRepository
                .findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(category -> modelMapper.map(category, ProductCategoryDto.class))
                .toList();
    }

    @Override
    public ProductCategory getProductCategoryOrThrow(Long id) {
        return productCategoryRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.CATEGORY_NOT_FOUND.getDescription()));
    }
}
