package de.ait.smallBusiness_be;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.products.dao.ProductCategoryRepository;
import de.ait.smallBusiness_be.products.dto.NewProductCategoryDto;
import de.ait.smallBusiness_be.products.dto.ProductCategoryDto;
import de.ait.smallBusiness_be.products.model.ProductCategory;
import de.ait.smallBusiness_be.products.service.impl.ProductCategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ProductCategoryServiceImplTest {

    @InjectMocks
    private ProductCategoryServiceImpl service;

    @Mock
    private ProductCategoryRepository repository;

    @Mock
    private ModelMapper modelMapper;

    // ===== ADD =====
    @Test
    void addProductCategory_success() {
        NewProductCategoryDto newDto = new NewProductCategoryDto("Books", "BKS");
        ProductCategory entity = new ProductCategory();
        entity.setName("BOOKS");
        entity.setArtName("BKS");
        ProductCategoryDto dto = new ProductCategoryDto();

        when(repository.findByNameIgnoreCaseOrArtNameIgnoreCase("BOOKS", "BKS"))
                .thenReturn(Collections.emptyList());
        when(repository.save(any(ProductCategory.class))).thenReturn(entity);
        when(modelMapper.map(entity, ProductCategoryDto.class)).thenReturn(dto);

        ProductCategoryDto result = service.addProductCategory(newDto);

        assertThat(result).isNotNull();
        verify(repository).save(any(ProductCategory.class));
    }

    @Test
    void addProductCategory_duplicate_throws() {
        NewProductCategoryDto newDto = new NewProductCategoryDto("Books", "BKS");
        ProductCategory existing = new ProductCategory();
        when(repository.findByNameIgnoreCaseOrArtNameIgnoreCase("BOOKS", "BKS"))
                .thenReturn(List.of(existing));

        assertThatThrownBy(() -> service.addProductCategory(newDto))
                .isInstanceOf(RestApiException.class)
                .hasMessageContaining("Category already exists");
    }

    // ===== GET BY ID =====
    @Test
    void getProductCategoryById_success() {
        ProductCategory entity = new ProductCategory();
        ProductCategoryDto dto = new ProductCategoryDto();
        when(repository.findById(1)).thenReturn(Optional.of(entity));
        when(modelMapper.map(entity, ProductCategoryDto.class)).thenReturn(dto);

        ProductCategoryDto result = service.getProductCategoryById(1);

        assertThat(result).isNotNull();
    }

    @Test
    void getProductCategoryById_notFound_throws() {
        when(repository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProductCategoryById(1))
                .isInstanceOf(RestApiException.class)
                .hasMessageContaining("Category not found");
    }

    // ===== DELETE =====
    @Test
    void deleteProductCategoryById_success() {
        ProductCategory entity = new ProductCategory();
        when(repository.findById(1)).thenReturn(Optional.of(entity));

        service.deleteProductCategoryById(1);

        verify(repository).delete(entity);
        verify(repository).flush();
    }

    @Test
    void deleteProductCategoryById_dataIntegrityViolation_throws() {
        ProductCategory entity = new ProductCategory();
        when(repository.findById(1)).thenReturn(Optional.of(entity));
        doThrow(DataIntegrityViolationException.class).when(repository).delete(entity);

        assertThatThrownBy(() -> service.deleteProductCategoryById(1))
                .isInstanceOf(RestApiException.class)
                .hasMessageContaining("Category cannot be deleted because it is used in other records");
    }

    // ===== UPDATE =====
    @Test
    void updateProductCategory_success() {
        NewProductCategoryDto newDto = new NewProductCategoryDto("Books", "BKS");
        ProductCategory entity = new ProductCategory();
        entity.setId(1);
        ProductCategoryDto dto = new ProductCategoryDto();

        when(repository.findById(1)).thenReturn(Optional.of(entity));
        when(repository.findByNameIgnoreCaseOrArtNameIgnoreCase("BOOKS", "BKS")).thenReturn(Collections.emptyList());
        when(repository.save(entity)).thenReturn(entity);
        when(modelMapper.map(entity, ProductCategoryDto.class)).thenReturn(dto);

        ProductCategoryDto result = service.updateProductCategory(1, newDto);

        assertThat(result).isNotNull();
        verify(repository).save(entity);
    }

    @Test
    void updateProductCategory_duplicate_throws() {
        NewProductCategoryDto newDto = new NewProductCategoryDto("Books", "BKS");
        ProductCategory entity = new ProductCategory();
        entity.setId(1);
        ProductCategory other = new ProductCategory();
        other.setId(2);

        when(repository.findById(1)).thenReturn(Optional.of(entity));
        when(repository.findByNameIgnoreCaseOrArtNameIgnoreCase("BOOKS", "BKS")).thenReturn(List.of(other));

        assertThatThrownBy(() -> service.updateProductCategory(1, newDto))
                .isInstanceOf(RestApiException.class)
                .hasMessageContaining("Category already exists");
    }

    // ===== FIND ALL =====
    @Test
    void findAllProductCategories_success() {
        ProductCategory entity = new ProductCategory();
        ProductCategoryDto dto = new ProductCategoryDto();

        when(repository.findAll()).thenReturn(List.of(entity));
        when(modelMapper.map(entity, ProductCategoryDto.class)).thenReturn(dto);

        List<ProductCategoryDto> result = service.findAllProductCategories();

        assertThat(result).hasSize(1);
    }

    @Test
    void findAllProductCategories_empty_returnsEmptyList() {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        List<ProductCategoryDto> result = service.findAllProductCategories();

        assertThat(result).isEmpty();
    }
}
