package de.ait.smallBusiness_be.products.service.impl;

import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.products.dao.ProductRepository;
import de.ait.smallBusiness_be.products.dto.NewProductDto;
import de.ait.smallBusiness_be.products.dto.ProductDto;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
    public ProductDto addProduct(NewProductDto newProductDto) {

        boolean exists = productRepository.existsByNameAndArticle(newProductDto.name(), newProductDto.article());
        if (exists) {
            throw new RestApiException(ErrorDescription.PRODUCT_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }
        Product product = modelMapper.map(newProductDto, Product.class);
        Product savedProduct = productRepository.save(product);
        return modelMapper.map(savedProduct, ProductDto.class);

    }

    @Override
    public ProductDto getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RestApiException(ErrorDescription.PRODUCT_NOT_FOUND, HttpStatus.CONFLICT));
        return modelMapper.map(product, ProductDto.class);
    }

    @Override
    public ProductDto deleteProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RestApiException(ErrorDescription.PRODUCT_NOT_FOUND, HttpStatus.CONFLICT));
        productRepository.delete(product);
        return modelMapper.map(product, ProductDto.class);
    }

    @Override
    public ProductDto updateProduct(Long id, NewProductDto newProductDto){

    Product product = productRepository.findById(id)
            .orElseThrow(() -> new RestApiException(ErrorDescription.PRODUCT_NOT_FOUND, HttpStatus.CONFLICT));
        product.setName(newProductDto.name());
        product.setArticle(newProductDto.article());
        product.setPurchasingPrice(newProductDto.purchasingPrice());
        product.setSellingPrice(newProductDto.sellingPrice());
        product.setUnitOfMeasurement(newProductDto.unitOfMeasurement());
        product.setWeight(newProductDto.weight());
        product.setSize(newProductDto.size());
        product.setProductCategory(newProductDto.productCategory());
        product.setDescription(newProductDto.description());
        product.setCustomsNumber(newProductDto.customsNumber());
        product.setDateOfLastPurchase(newProductDto.dateOfLastPurchase());

        Product updatedProduct = productRepository.save(product);
        return modelMapper.map(updatedProduct, ProductDto.class);
    }

    @Override
    public List<ProductDto> findAllProducts() {

        List<Product> products = productRepository.findAll();
            return products.stream()
                .map(product -> modelMapper.map(product, ProductDto.class))
                .collect(Collectors.toList());
    }
}
