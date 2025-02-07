package de.ait.smallBusiness_be.products.service.impl;

import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.products.dao.ProductRepository;
import de.ait.smallBusiness_be.products.dto.NewDimensionsDto;
import de.ait.smallBusiness_be.products.dto.NewProductDto;
import de.ait.smallBusiness_be.products.dto.UpdateProductDto;
import de.ait.smallBusiness_be.products.dto.ProductDto;
import de.ait.smallBusiness_be.products.model.Dimensions;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.products.model.UnitOfMeasurement;
import de.ait.smallBusiness_be.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 11/15/2024
 * Small_business_BE
 *
 * @author Chechkina (AIT TR)
 */

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    final ProductRepository productRepository;
    final ModelMapper modelMapper;

    @Override
    @Transactional
    public ProductDto addProduct(NewProductDto newProductDto) {

        boolean exists = productRepository
                .existsByNameAndVendorArticleAndPurchasingPriceAndProductCategory(
                        newProductDto.getName(),
                        newProductDto.getVendorArticle(),
                        newProductDto.getPurchasingPrice(),
                        newProductDto.getProductCategory()
                );
        if (exists) {
            throw new RestApiException(ErrorDescription.PRODUCT_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        Product product = modelMapper.map(newProductDto, Product.class);
        Product savedProduct = productRepository.save(product);
        String art = newProductDto.getProductCategory().getArtName() + "-" + savedProduct.getId();
        savedProduct.setArticle(art);
        productRepository.save(savedProduct);

        return modelMapper.map(savedProduct, ProductDto.class);

    }

    @Override
    public ProductDto getProductById(Long id) {

        Product product = getProductOrThrow(id);
        return modelMapper.map(product, ProductDto.class);
    }

    @Override
    public void deleteProductById(Long id) {

        Product product = getProductOrThrow(id);
        productRepository.delete(product);
    }

    @Override
    @Transactional
    public ProductDto updateProduct(Long id, UpdateProductDto updateProductDto){

    Product product = getProductOrThrow(id);
        product.setName(updateProductDto.getName());
        product.setArticle(updateProductDto.getArticle());
        product.setPurchasingPrice(updateProductDto.getPurchasingPrice());
        product.setSellingPrice(updateProductDto.getSellingPrice());

        if (updateProductDto.getUnitOfMeasurement() != null) {
            try {
                UnitOfMeasurement unit = UnitOfMeasurement.valueOf(updateProductDto.getUnitOfMeasurement().toUpperCase());
                product.setUnitOfMeasurement(unit);
            } catch (IllegalArgumentException e) {
                throw new RestApiException(ErrorDescription.INVALID_UNIT_OF_MEASUREMENT, HttpStatus.BAD_REQUEST);
            }
        }

        //product.setWeight(updateProductDto.getWeight());

        if (updateProductDto.getNewDimensions() != null) {
            Dimensions dimensions = modelMapper.map(updateProductDto.getNewDimensions(), Dimensions.class);
            product.setDimensions(dimensions);
        }
        product.setProductCategory(updateProductDto.getProductCategory());
        product.setDescription(updateProductDto.getDescription());
        product.setCustomsNumber(updateProductDto.getCustomsNumber());
        //product.setDateOfLastPurchase(updateProductDto.getDateOfLastPurchase());
        product.setLastModifiedDate(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);
        ProductDto productDto = modelMapper.map(updatedProduct, ProductDto.class);
        NewDimensionsDto updatedDimensions = modelMapper.map(updatedProduct.getDimensions(), NewDimensionsDto.class);
        productDto.setNewDimensions(updatedDimensions);
        return productDto;
    }

    @Override
    public Page<ProductDto> findAllProducts(Pageable pageable) {

        Page<Product> productsPage = productRepository.findAll(pageable);
        if (productsPage.isEmpty()) {
            throw new RestApiException(ErrorDescription.LIST_PRODUCTS_IS_EMPTY, HttpStatus.NOT_FOUND);
        }

        return productsPage.map(product -> modelMapper.map(product, ProductDto.class));
    }

    @Override
    public Page<ProductDto> findProductsByCategoryId(int categoryId, Pageable pageable) {

        Page<Product> productsPage = productRepository.findByProductCategory_Id(categoryId, pageable);
        if (productsPage.isEmpty()) {
            throw new RestApiException(ErrorDescription.LIST_PRODUCTS_IS_EMPTY, HttpStatus.NOT_FOUND);
        }

        return productsPage.map(product -> modelMapper.map(product, ProductDto.class));
    }

    public Product getProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RestApiException(ErrorDescription.PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND));
    }
}
