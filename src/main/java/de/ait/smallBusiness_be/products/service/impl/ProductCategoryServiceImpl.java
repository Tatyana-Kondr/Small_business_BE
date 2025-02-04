package de.ait.smallBusiness_be.products.service.impl;


import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.products.dao.ProductCategoryRepository;
import de.ait.smallBusiness_be.products.dto.NewProductCategoryDto;
import de.ait.smallBusiness_be.products.dto.ProductCategoryDto;
import de.ait.smallBusiness_be.products.model.ProductCategory;
import de.ait.smallBusiness_be.products.service.ProductCategoryService;
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
        // Приводим к верхнему регистру
        String nameToUpperCase = newProductCategoryDto.getName().toUpperCase();
        String artNameToUpperCase = newProductCategoryDto.getArtName().toUpperCase();

        // Проверяем, есть ли уже категория с таким же именем или артикулом
        List<ProductCategory> existingCategories = productCategoryRepository
                .findByNameIgnoreCaseOrArtNameIgnoreCase(nameToUpperCase, artNameToUpperCase);

        if (!existingCategories.isEmpty()) {
            throw new RestApiException(ErrorDescription.CATEGORY_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        // Создаем новую категорию и сохраняем
        ProductCategory productCategory = new ProductCategory();
        productCategory.setName(nameToUpperCase);
        productCategory.setArtName(artNameToUpperCase);

        ProductCategory savedCategory = productCategoryRepository.save(productCategory);

        return modelMapper.map(savedCategory, ProductCategoryDto.class);
    }

    @Override
    public ProductCategoryDto getProductCategoryById(Integer id) {
        ProductCategory productCategory = getProductCategoryOrThrow(id);

        return modelMapper.map(productCategory, ProductCategoryDto.class);
    }

    @Override
    public ProductCategoryDto deleteProductCategoryById(Integer id) {
        ProductCategory productCategory = getProductCategoryOrThrow(id);

        productCategoryRepository.delete(productCategory);

        return modelMapper.map(productCategory, ProductCategoryDto.class);
    }

    @Override
    public ProductCategoryDto updateProductCategory(Integer id, NewProductCategoryDto newProductCategoryDto) {
        ProductCategory productCategory = getProductCategoryOrThrow(id);

        // Приводим к верхнему регистру
        String nameToUpperCase = newProductCategoryDto.getName().toUpperCase();
        String artNameToUpperCase = newProductCategoryDto.getArtName().toUpperCase();

        // Получаем список всех категорий с таким же name или artName
        List<ProductCategory> existingCategories = productCategoryRepository
                .findByNameIgnoreCaseOrArtNameIgnoreCase(nameToUpperCase, artNameToUpperCase);

        // Проверяем, есть ли в этом списке категории с другим id
        boolean hasDuplicate = existingCategories.stream()
                .anyMatch(cat -> !cat.getId().equals(id));

        if (hasDuplicate) {
            throw new RestApiException(ErrorDescription.CATEGORY_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        // Обновляем поля
        productCategory.setName(nameToUpperCase);
        productCategory.setArtName(artNameToUpperCase);

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

    private ProductCategory getProductCategoryOrThrow(Integer id) {
        return productCategoryRepository
                .findById(id)
                .orElseThrow(() -> new RestApiException(ErrorDescription.CATEGORY_NOT_FOUND, HttpStatus.NOT_FOUND));
    }
}
