package de.ait.smallBusiness_be.products.service.impl;

import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.products.dao.ProductRepository;
import de.ait.smallBusiness_be.products.dao.UnitOfMeasurementRepository;
import de.ait.smallBusiness_be.products.dto.*;
import de.ait.smallBusiness_be.products.model.Dimensions;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.products.model.UnitOfMeasurement;
import de.ait.smallBusiness_be.products.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
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

     private final ProductRepository productRepository;
     private final UnitOfMeasurementRepository unitOfMeasurementRepository;
     private final ModelMapper modelMapper;

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
         UnitOfMeasurement unit = unitOfMeasurementRepository.findById(newProductDto.getUnitOfMeasurementId())
                 .orElseThrow(() -> new EntityNotFoundException("Unit Of Measurement not found"));

        Product product = modelMapper.map(newProductDto, Product.class);
        product.setUnitOfMeasurement(unit);
        Product savedProduct = productRepository.save(product);
        String art = newProductDto.getProductCategory().getArtName() + "-" + savedProduct.getId();
        savedProduct.setArticle(art);
        productRepository.save(savedProduct);

        return modelMapper.map(savedProduct, ProductDto.class);
    }

    @Override
    @Transactional
    public ProductDto getProductById(Long id) {

        Product product = getProductOrThrow(id);
        ProductDto productDto = modelMapper.map(product, ProductDto.class);

        if (product.getDimensions() != null) {
            NewDimensionsDto dimensionsDto = modelMapper.map(product.getDimensions(), NewDimensionsDto.class);
            productDto.setNewDimensions(dimensionsDto);
        }
        return productDto;
    }

    @Override
    @Transactional
    public void deleteProductById(Long id) {
        try {
            productRepository.deleteById(id);
            productRepository.flush(); //сразу отправляем изменения в базу, чтобы исключение возникло здесь, если есть ограничения.
        } catch (DataIntegrityViolationException e) {
            throw new RestApiException(ErrorDescription.PRODUCT_DELETE_FAILED, HttpStatus.CONFLICT);
        }
    }

    @Override
    @Transactional
    public ProductDto updateProduct(Long id, UpdateProductDto updateProductDto){

    Product product = getProductOrThrow(id);
        product.setName(updateProductDto.getName());
        product.setArticle(updateProductDto.getArticle());
        product.setPurchasingPrice(updateProductDto.getPurchasingPrice());
        product.setMarkupPercentage(updateProductDto.getMarkupPercentage());
        product.setSellingPrice(updateProductDto.getSellingPrice());

        if (updateProductDto.getUnitOfMeasurementId() != null) {
            try {
                UnitOfMeasurement unit = unitOfMeasurementRepository.findById(updateProductDto.getUnitOfMeasurementId())
                        .orElseThrow(() -> new EntityNotFoundException("Unit Of Measurement not found"));
                product.setUnitOfMeasurement(unit);
            } catch (IllegalArgumentException e) {
                throw new RestApiException(ErrorDescription.INVALID_UNIT_OF_MEASUREMENT, HttpStatus.BAD_REQUEST);
            }
        }

        product.setWeight(updateProductDto.getWeight());

        if (updateProductDto.getNewDimensions() != null) {
            Dimensions dimensions = modelMapper.map(updateProductDto.getNewDimensions(), Dimensions.class);
            product.setDimensions(dimensions);
        }
        product.setProductCategory(updateProductDto.getProductCategory());
        product.setDescription(updateProductDto.getDescription());
        product.setCustomsNumber(updateProductDto.getCustomsNumber());
        product.setStorageLocation(updateProductDto.getStorageLocation());
        //product.setDateOfLastPurchase(updateProductDto.getDateOfLastPurchase());
        product.setLastModifiedDate(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);
        ProductDto productDto = modelMapper.map(updatedProduct, ProductDto.class);
        NewDimensionsDto updatedDimensions = modelMapper.map(updatedProduct.getDimensions(), NewDimensionsDto.class);
        productDto.setNewDimensions(updatedDimensions);
        return productDto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> findProducts(String searchTerm, Pageable pageable) {
        Page<Product> productsPage;

        if (StringUtils.hasText(searchTerm)) {
            productsPage = productRepository.searchProductsPage(searchTerm, pageable);
        } else {
            productsPage = productRepository.findAll(pageable);
        }

        if (productsPage.isEmpty()) {
            throw new RestApiException(ErrorDescription.LIST_PRODUCTS_IS_EMPTY, HttpStatus.NOT_FOUND);
        }

        return mapToProductDtoPage(productsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getAllProducts(String searchTerm) {
        List<Product> products = StringUtils.hasText(searchTerm)
                ? productRepository.searchProducts(searchTerm)
                : productRepository.findAll();

        if (products.isEmpty()) {
            throw new RestApiException(ErrorDescription.LIST_PRODUCTS_IS_EMPTY, HttpStatus.NOT_FOUND);
        }
        return mapToProductDtoList(products);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> findProductsByCategoryId(int categoryId, String searchTerm, Pageable pageable) {
        Page<Product> productsPage;

        if (StringUtils.hasText(searchTerm)) {
            productsPage = productRepository.searchProductsByCategoryPage(categoryId, searchTerm, pageable);
        } else {
            productsPage = productRepository.findByProductCategory_Id(categoryId, pageable);
        }

        if (productsPage.isEmpty()) {
            throw new RestApiException(ErrorDescription.LIST_PRODUCTS_IS_EMPTY, HttpStatus.NOT_FOUND);
        }

        return mapToProductDtoPage(productsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getAllProductsByCategoryId(int categoryId, String searchTerm) {
        List<Product> products;

        if (StringUtils.hasText(searchTerm)) {
            products = productRepository.searchProductsByCategory(categoryId, searchTerm);
        } else {
            products = productRepository.findAllByProductCategory_Id(categoryId);
        }

        if (products.isEmpty()) {
            throw new RestApiException(ErrorDescription.LIST_PRODUCTS_IS_EMPTY, HttpStatus.NOT_FOUND);
        }

        return products.stream().map(productDto -> modelMapper.map(productDto, ProductDto.class)).collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public ProductDto findProductByArticle(String article) {
        Product product = productRepository.findProductByArticle(article)
                .orElseThrow(() -> new RestApiException(ErrorDescription.PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND));

        return mapToProductDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> findProductsByVendorArticle(String vendorArticle) {
        List<Product> products = productRepository.findProductsByVendorArticle(vendorArticle);
        if (products.isEmpty()) {
            throw new RestApiException(ErrorDescription.LIST_PRODUCTS_IS_EMPTY, HttpStatus.NOT_FOUND);
        }
        return mapToProductDtoList(products);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> findProductsByName(String name) {
        List<Product> products = productRepository.findProductsByName(name);
        if (products.isEmpty()) {
            throw new RestApiException(ErrorDescription.LIST_PRODUCTS_IS_EMPTY, HttpStatus.NOT_FOUND);
        }
        return mapToProductDtoList(products);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductPickDto> pick(String searchTerm, Long categoryId, Integer limit) {
        String q = (searchTerm == null) ? "" : searchTerm.trim();
        int safeLimit = (limit == null) ? 50 : Math.min(Math.max(limit, 10), 100);

        // Если нет категории и строка меньше 2 символов — не ищем
        if (categoryId == null && q.length() < 2) {
            return List.of();
        }

        return productRepository.pickProducts(q, categoryId, PageRequest.of(0, safeLimit));
    }

    public Product getProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RestApiException(ErrorDescription.PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private Page<ProductDto> mapToProductDtoPage(Page<Product> productsPage) {

        return productsPage.map(product -> {
            ProductDto productDto = modelMapper.map(product, ProductDto.class);

            if (product.getDimensions() != null) {
                NewDimensionsDto dimensionsDto = modelMapper.map(product.getDimensions(), NewDimensionsDto.class);
                productDto.setNewDimensions(dimensionsDto);
            }

            return productDto;
        });
    }

    private ProductDto mapToProductDto(Product product) {
        ProductDto productDto = modelMapper.map(product, ProductDto.class);

        if (product.getDimensions() != null) {
            NewDimensionsDto dimensionsDto = modelMapper.map(product.getDimensions(), NewDimensionsDto.class);
            productDto.setNewDimensions(dimensionsDto);
        }

        return productDto;
    }

    private List<ProductDto> mapToProductDtoList(List<Product> products) {
        return products.stream()
                .map(this::mapToProductDto)
                .toList();
    }
}
