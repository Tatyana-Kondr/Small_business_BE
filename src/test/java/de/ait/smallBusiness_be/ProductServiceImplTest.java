package de.ait.smallBusiness_be;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.products.dao.ProductRepository;
import de.ait.smallBusiness_be.products.dto.NewDimensionsDto;
import de.ait.smallBusiness_be.products.dto.NewProductDto;
import de.ait.smallBusiness_be.products.dto.ProductDto;
import de.ait.smallBusiness_be.products.dto.UpdateProductDto;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.products.model.ProductCategory;
import de.ait.smallBusiness_be.products.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @InjectMocks
    private ProductServiceImpl service;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ModelMapper modelMapper;

    // ===== ADD PRODUCT =====
    @Test
    void addProduct_success() {
        NewProductDto newDto = new NewProductDto();
        newDto.setName("Product1");
        newDto.setVendorArticle("VA123");
        newDto.setPurchasingPrice(BigDecimal.valueOf(10));
        newDto.setUnitOfMeasurement("ST");
        newDto.setProductCategory(new ProductCategory());

        Product product = new Product();
        product.setId(1L);
        ProductDto dto = new ProductDto();

        when(productRepository.existsByNameAndVendorArticleAndPurchasingPriceAndProductCategory(
                any(), any(), any(), any())).thenReturn(false);
        when(modelMapper.map(newDto, Product.class)).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(modelMapper.map(product, ProductDto.class)).thenReturn(dto);

        ProductDto result = service.addProduct(newDto);

        assertThat(result).isNotNull();
        verify(productRepository, times(2)).save(product); // один раз с article
    }

    @Test
    void addProduct_duplicate_throws() {
        NewProductDto newDto = new NewProductDto();
        when(productRepository.existsByNameAndVendorArticleAndPurchasingPriceAndProductCategory(
                any(), any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> service.addProduct(newDto))
                .isInstanceOf(RestApiException.class)
                .hasMessageContaining("Product with the same name and article already exists.");
    }

    // ===== GET PRODUCT =====
    @Test
    void getProductById_success() {
        Product product = new Product();
        ProductDto dto = new ProductDto();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(modelMapper.map(product, ProductDto.class)).thenReturn(dto);

        ProductDto result = service.getProductById(1L);

        assertThat(result).isNotNull();
    }

    @Test
    void getProductById_notFound_throws() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProductById(1L))
                .isInstanceOf(RestApiException.class)
                .hasMessageContaining("Product not found");
    }

    // ===== DELETE PRODUCT =====
    @Test
    void deleteProductById_success() {
        doNothing().when(productRepository).deleteById(1L);

        service.deleteProductById(1L);

        verify(productRepository).deleteById(1L);
        verify(productRepository).flush();
    }

    @Test
    void deleteProductById_dataIntegrityViolation_throws() {
        doThrow(DataIntegrityViolationException.class).when(productRepository).deleteById(1L);

        assertThatThrownBy(() -> service.deleteProductById(1L))
                .isInstanceOf(RestApiException.class)
                .hasMessageContaining( "Product cannot be deleted because it is used in other records.");
    }

    // ===== UPDATE PRODUCT =====
    @Test
    void updateProduct_success() {
        Product product = new Product();
        UpdateProductDto dto = new UpdateProductDto();
        ProductDto resultDto = new ProductDto();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(modelMapper.map(product, ProductDto.class)).thenReturn(resultDto);
        when(modelMapper.map(any(), eq(NewDimensionsDto.class))).thenReturn(new NewDimensionsDto());

        ProductDto result = service.updateProduct(1L, dto);

        assertThat(result).isNotNull();
        verify(productRepository).save(product);
    }

    // ===== FIND PRODUCTS =====
    @Test
    void findProducts_withSearchTerm_success() {
        Page<Product> page = new PageImpl<>(List.of(new Product()));
        Page<ProductDto> dtoPage = new PageImpl<>(List.of(new ProductDto()));

        when(productRepository.searchProducts(anyString(), any(Pageable.class))).thenReturn(page);
        when(modelMapper.map(any(Product.class), eq(ProductDto.class))).thenReturn(new ProductDto());

        Page<ProductDto> result = service.findProducts("test", Pageable.unpaged());

        assertThat(result).isNotNull();
    }

    @Test
    void findProducts_empty_throws() {
        Page<Product> emptyPage = Page.empty();
        when(productRepository.findAll(Pageable.unpaged())).thenReturn(emptyPage);

        assertThatThrownBy(() -> service.findProducts("", Pageable.unpaged()))
                .isInstanceOf(RestApiException.class)
                .hasMessageContaining("List of products is empty");
    }
}
