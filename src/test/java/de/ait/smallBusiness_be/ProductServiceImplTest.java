package de.ait.smallBusiness_be;

import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.productions.dao.ProductionItemRepository;
import de.ait.smallBusiness_be.productions.dao.ProductionRepository;
import de.ait.smallBusiness_be.products.dao.ProductPhotoRepository;
import de.ait.smallBusiness_be.products.dao.ProductRepository;
import de.ait.smallBusiness_be.products.dto.*;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.products.model.ProductCategory;
import de.ait.smallBusiness_be.products.model.UnitOfMeasurement;
import de.ait.smallBusiness_be.products.service.ProductCategoryService;
import de.ait.smallBusiness_be.products.service.UnitOfMeasurementService;
import de.ait.smallBusiness_be.products.service.impl.ProductServiceImpl;
import de.ait.smallBusiness_be.purchases.dao.PurchaseItemRepository;
import de.ait.smallBusiness_be.purchases.dao.PurchaseRepository;
import de.ait.smallBusiness_be.sales.dao.SaleItemRepository;
import de.ait.smallBusiness_be.warehouse.dao.WarehouseRecordRepository;
import de.ait.smallBusiness_be.warehouse.dao.WarehouseRepository;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @InjectMocks
    private ProductServiceImpl service;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductCategoryService productCategoryService;

    @Mock
    private UnitOfMeasurementService unitOfMeasurementService;

    @Mock
    private ProductPhotoRepository productPhotoRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private SaleItemRepository saleItemRepository;

    @Mock
    private PurchaseItemRepository purchaseItemRepository;

    @Mock
    private ProductionRepository productionRepository;

    @Mock
    private ProductionItemRepository productionItemRepository;

    @Mock
    private WarehouseRecordRepository warehouseRecordRepository;

    @Mock
    private ModelMapper modelMapper;


    @Test
    void addProduct_success() {
        NewProductDto newDto = new NewProductDto();
        newDto.setName("Product1");
        newDto.setVendorArticle("VA123");
        newDto.setPurchasingPrice(BigDecimal.valueOf(10));
        newDto.setProductCategoryId(2L);
        newDto.setUnitOfMeasurementId(1L);

        ProductCategory category = new ProductCategory();
        category.setId(2L);
        category.setArtName("PR");

        UnitOfMeasurement unit = new UnitOfMeasurement();
        unit.setId(1L);
        unit.setName("ST");

        Product product = new Product();

        Product savedProduct = new Product();
        savedProduct.setId(10L);

        ProductDto resultDto = new ProductDto();
        resultDto.setId(10L);

        when(productCategoryService.getProductCategoryOrThrow(2L)).thenReturn(category);
        when(unitOfMeasurementService.getUnitOfMeasurementOrThrow(1L)).thenReturn(unit);
        when(modelMapper.map(newDto, Product.class)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(savedProduct);
        when(productRepository.save(savedProduct)).thenReturn(savedProduct);
        when(modelMapper.map(savedProduct, ProductDto.class)).thenReturn(resultDto);

        ProductDto result = service.addProduct(newDto);

        assertThat(result).isSameAs(resultDto);
        assertThat(savedProduct.getArticle()).isEqualTo("PR-10");

        verify(productCategoryService).getProductCategoryOrThrow(2L);
        verify(unitOfMeasurementService).getUnitOfMeasurementOrThrow(1L);
        verify(productRepository).save(product);
        verify(productRepository).save(savedProduct);
        verify(modelMapper).map(savedProduct, ProductDto.class);
    }

    @Test
    void getProductById_success() {
        Product product = new Product();
        product.setId(1L);

        ProductDto dto = new ProductDto();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(modelMapper.map(product, ProductDto.class)).thenReturn(dto);
        when(productPhotoRepository.findOrderedByProductId(1L)).thenReturn(List.of());
        when(warehouseRepository.findByProduct(product)).thenReturn(Optional.empty());

        ProductDto result = service.getProductById(1L);

        assertThat(result).isSameAs(dto);
        assertThat(result.getHasPhoto()).isFalse();
        assertThat(result.getQuantity()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(productRepository).findById(1L);
    }

    @Test
    void getProductById_notFound_throws() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProductById(1L))
                .isInstanceOf(RestApiException.class)
                .hasMessageContaining("Product not found");

        verify(productRepository).findById(1L);
        verifyNoInteractions(productPhotoRepository);
        verifyNoInteractions(warehouseRepository);
    }

    @Test
    void deleteProductById_success() {
        Product product = new Product();
        product.setId(1L);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        service.deleteProductById(1L);

        verify(productRepository).findById(1L);
        verify(productRepository).delete(product);
        verify(productRepository).flush();
    }

    @Test
    void deleteProductById_dataIntegrityViolation_throws() {
        Product product = new Product();
        product.setId(1L);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        doThrow(new DataIntegrityViolationException("constraint"))
                .when(productRepository)
                .delete(product);

        assertThatThrownBy(() -> service.deleteProductById(1L))
                .isInstanceOf(RestApiException.class)
                .hasMessageContaining(
                        "Product cannot be deleted because it is used in other records."
                );

        verify(productRepository).delete(product);
        verify(productRepository, never()).flush();
    }


    // ===== UPDATE PRODUCT =====

    @Test
    void updateProduct_success() {
        Product product = new Product();
        product.setId(1L);

        ProductCategory category = new ProductCategory();
        category.setId(2L);

        UnitOfMeasurement unit = new UnitOfMeasurement();
        unit.setId(3L);

        UpdateProductDto updateDto = new UpdateProductDto();
        updateDto.setName("Updated Product");
        updateDto.setArticle("ART-100");
        updateDto.setVendorArticle("VA-100");
        updateDto.setPurchasingPrice(BigDecimal.valueOf(10));
        updateDto.setMarkupPercentage(BigDecimal.valueOf(20));
        updateDto.setSellingPrice(BigDecimal.valueOf(12));
        updateDto.setProductCategoryId(2L);
        updateDto.setUnitOfMeasurementId(3L);
        updateDto.setWeight(BigDecimal.valueOf(1.250));
        updateDto.setDescription("Description");
        updateDto.setCustomsNumber("CN123");
        updateDto.setStorageLocation("A-01");

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(productCategoryService.getProductCategoryOrThrow(2L))
                .thenReturn(category);

        when(unitOfMeasurementService.getUnitOfMeasurementOrThrow(3L))
                .thenReturn(unit);

        when(productRepository.save(product))
                .thenReturn(product);

        ProductDto resultDto = new ProductDto();

        when(modelMapper.map(product, ProductDto.class))
                .thenReturn(resultDto);

        when(productPhotoRepository.findOrderedByProductId(1L))
                .thenReturn(List.of());

        when(warehouseRepository.findByProduct(product))
                .thenReturn(Optional.empty());

        ProductDto result = service.updateProduct(1L, updateDto);

        assertThat(result).isSameAs(resultDto);

        assertThat(product.getName()).isEqualTo("Updated Product");
        assertThat(product.getArticle()).isEqualTo("ART-100");
        assertThat(product.getVendorArticle()).isEqualTo("VA-100");
        assertThat(product.getProductCategory()).isSameAs(category);
        assertThat(product.getUnitOfMeasurement()).isSameAs(unit);
        assertThat(product.getDimensions()).isNull();

        verify(productRepository).save(product);
    }


    // ===== FIND PRODUCTS =====

    @Test
    void findProducts_withSearchTerm_success() {
        Product product = new Product();
        product.setId(1L);

        Page<Product> page = new PageImpl<>(List.of(product));

        when(productRepository.searchProductsPage(
                eq("test"),
                any(Pageable.class)
        )).thenReturn(page);

        when(productPhotoRepository.findProductIdsWithPhotos(List.of(1L)))
                .thenReturn(List.of());

        when(warehouseRepository.findAllByProduct_IdIn(List.of(1L)))
                .thenReturn(List.of());

        when(modelMapper.map(product, ProductDto.class))
                .thenReturn(new ProductDto());

        Page<ProductDto> result =
                service.findProducts("test", Pageable.unpaged());

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        assertThat(result.getContent().get(0).getHasPhoto())
                .isFalse();

        assertThat(result.getContent().get(0).getQuantity())
                .isEqualByComparingTo(BigDecimal.ZERO);

        verify(productRepository)
                .searchProductsPage(eq("test"), any(Pageable.class));
    }

    @Test
    void findProducts_withoutSearchTerm_usesFindAll() {
        Page<Product> page = Page.empty();

        when(productRepository.findAll(Pageable.unpaged()))
                .thenReturn(page);

        Page<ProductDto> result =
                service.findProducts("", Pageable.unpaged());

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();

        verify(productRepository)
                .findAll(Pageable.unpaged());

        verify(productRepository, never())
                .searchProductsPage(anyString(), any(Pageable.class));
    }

    @Test
    void findProducts_empty_returnsEmptyPage() {
        Page<Product> emptyPage = Page.empty();

        when(productRepository.findAll(Pageable.unpaged()))
                .thenReturn(emptyPage);

        Page<ProductDto> result =
                service.findProducts("", Pageable.unpaged());

        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isTrue();

        verifyNoInteractions(productPhotoRepository);
        verifyNoInteractions(warehouseRepository);
    }


    // ===== FIND PRODUCTS BY CATEGORY =====

    @Test
    void findProductsByCategoryId_success() {
        Product product = new Product();
        product.setId(1L);

        Page<Product> page = new PageImpl<>(List.of(product));

        when(productRepository.findByProductCategory_Id(
                2L,
                Pageable.unpaged()
        )).thenReturn(page);

        when(productPhotoRepository.findProductIdsWithPhotos(List.of(1L)))
                .thenReturn(List.of());

        when(warehouseRepository.findAllByProduct_IdIn(List.of(1L)))
                .thenReturn(List.of());

        when(modelMapper.map(product, ProductDto.class))
                .thenReturn(new ProductDto());

        Page<ProductDto> result =
                service.findProductsByCategoryId(
                        2L,
                        "",
                        Pageable.unpaged()
                );

        assertThat(result.getContent()).hasSize(1);

        verify(productRepository)
                .findByProductCategory_Id(
                        2L,
                        Pageable.unpaged()
                );
    }


    // ===== SIMILAR PRODUCTS =====

    @Test
    void findSimilarProducts_success() {
        ProductPickDto product = new ProductPickDto();

        when(productRepository.findSimilarProducts(
                "Product A",
                "VA-100"
        )).thenReturn(List.of(product));

        List<ProductPickDto> result =
                service.findSimilarProducts(
                        "  Product A  ",
                        "  VA-100  "
                );

        assertThat(result)
                .containsExactly(product);

        verify(productRepository)
                .findSimilarProducts(
                        "Product A",
                        "VA-100"
                );
    }

    @Test
    void findSimilarProducts_blankName_returnsEmptyList() {
        List<ProductPickDto> result =
                service.findSimilarProducts("   ", "VA-100");

        assertThat(result).isEmpty();

        verifyNoInteractions(productRepository);
    }

    @Test
    void findSimilarProducts_blankVendorArticle_passesNull() {
        when(productRepository.findSimilarProducts(
                "Product A",
                null
        )).thenReturn(List.of());

        List<ProductPickDto> result =
                service.findSimilarProducts(
                        "Product A",
                        "   "
                );

        assertThat(result).isEmpty();

        verify(productRepository)
                .findSimilarProducts(
                        "Product A",
                        null
                );
    }


    // ===== SIMILAR PRODUCTS FOR UPDATE =====

    @Test
    void findSimilarProductsForUpdate_success() {
        Product existingProduct = new Product();
        existingProduct.setId(1L);

        ProductPickDto similarProduct = new ProductPickDto();

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(existingProduct));

        when(productRepository.findSimilarProductsForUpdate(
                1L,
                "Product A",
                "ART-100",
                "VA-100"
        )).thenReturn(List.of(similarProduct));

        List<ProductPickDto> result =
                service.findSimilarProductsForUpdate(
                        1L,
                        "  Product A  ",
                        "  ART-100  ",
                        "  VA-100  "
                );

        assertThat(result)
                .containsExactly(similarProduct);

        verify(productRepository)
                .findById(1L);

        verify(productRepository)
                .findSimilarProductsForUpdate(
                        1L,
                        "Product A",
                        "ART-100",
                        "VA-100"
                );
    }

    @Test
    void findSimilarProductsForUpdate_blankValues_passesNull() {
        Product existingProduct = new Product();
        existingProduct.setId(1L);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(existingProduct));

        when(productRepository.findSimilarProductsForUpdate(
                1L,
                null,
                null,
                null
        )).thenReturn(List.of());

        List<ProductPickDto> result =
                service.findSimilarProductsForUpdate(
                        1L,
                        "   ",
                        "   ",
                        "   "
                );

        assertThat(result).isEmpty();

        verify(productRepository)
                .findSimilarProductsForUpdate(
                        1L,
                        null,
                        null,
                        null
                );
    }

    @Test
    void findSimilarProductsForUpdate_onlyChangedArticle() {
        Product existingProduct = new Product();
        existingProduct.setId(1L);

        ProductPickDto similarProduct = new ProductPickDto();

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(existingProduct));

        when(productRepository.findSimilarProductsForUpdate(
                1L,
                null,
                "ART-200",
                null
        )).thenReturn(List.of(similarProduct));

        List<ProductPickDto> result =
                service.findSimilarProductsForUpdate(
                        1L,
                        null,
                        "  ART-200  ",
                        null
                );

        assertThat(result)
                .containsExactly(similarProduct);

        verify(productRepository)
                .findSimilarProductsForUpdate(
                        1L,
                        null,
                        "ART-200",
                        null
                );
    }

    @Test
    void findSimilarProductsForUpdate_productNotFound_throws() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.findSimilarProductsForUpdate(
                        1L,
                        "Product A",
                        "ART-100",
                        "VA-100"
                )
        )
                .isInstanceOf(RestApiException.class)
                .hasMessageContaining("Product not found");

        verify(productRepository)
                .findById(1L);

        verify(productRepository, never())
                .findSimilarProductsForUpdate(
                        anyLong(),
                        any(),
                        any(),
                        any()
                );
    }
}