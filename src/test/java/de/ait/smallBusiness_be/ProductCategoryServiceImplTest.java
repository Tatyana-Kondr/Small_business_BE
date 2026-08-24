package de.ait.smallBusiness_be;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import de.ait.smallBusiness_be.exceptions.FieldValidationException;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.products.dao.ProductCategoryRepository;
import de.ait.smallBusiness_be.products.dto.NewProductCategoryDto;
import de.ait.smallBusiness_be.products.dto.ProductCategoryDto;
import de.ait.smallBusiness_be.products.model.ProductCategory;
import de.ait.smallBusiness_be.products.service.impl.ProductCategoryServiceImpl;
import de.ait.smallBusiness_be.validation.dto.ValidationErrorDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;

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

        when(repository.existsByNameIgnoreCase("BOOKS")).thenReturn(false);
        when(repository.existsByArtNameIgnoreCase("BKS")).thenReturn(false);
        when(repository.save(any(ProductCategory.class))).thenReturn(entity);
        when(modelMapper.map(entity, ProductCategoryDto.class)).thenReturn(dto);

        ProductCategoryDto result = service.addProductCategory(newDto);

        assertThat(result).isNotNull();
        verify(repository).existsByNameIgnoreCase("BOOKS");
        verify(repository).existsByArtNameIgnoreCase("BKS");
        verify(repository).save(any(ProductCategory.class));
    }

    @Test
    void addProductCategory_duplicateName_throwsFieldValidationException() {
        NewProductCategoryDto newDto =
                new NewProductCategoryDto("Books", "BKS");

        when(repository.existsByNameIgnoreCase("BOOKS"))
                .thenReturn(true);

        when(repository.existsByArtNameIgnoreCase("BKS"))
                .thenReturn(false);

        assertThatThrownBy(() -> service.addProductCategory(newDto))
                .isInstanceOf(FieldValidationException.class)
                .satisfies(exception -> {
                    FieldValidationException ex =
                            (FieldValidationException) exception;

                    assertThat(ex.getErrors()).hasSize(1);

                    assertThat(ex.getErrors().get(0).getField())
                            .isEqualTo("name");

                    assertThat(ex.getErrors().get(0).getRejectedValue())
                            .isEqualTo("BOOKS");

                    assertThat(ex.getErrors().get(0).getMessage())
                            .isEqualTo("Kategorie existiert bereits.");
                });

        verify(repository).existsByNameIgnoreCase("BOOKS");
        verify(repository).existsByArtNameIgnoreCase("BKS");
        verify(repository, never()).save(any(ProductCategory.class));
    }

    @Test
    void addProductCategory_duplicateArtName_throwsFieldValidationException() {
        NewProductCategoryDto newDto =
                new NewProductCategoryDto("Books", "BKS");

        when(repository.existsByNameIgnoreCase("BOOKS"))
                .thenReturn(false);

        when(repository.existsByArtNameIgnoreCase("BKS"))
                .thenReturn(true);

        assertThatThrownBy(() -> service.addProductCategory(newDto))
                .isInstanceOf(FieldValidationException.class)
                .satisfies(exception -> {
                    FieldValidationException ex =
                            (FieldValidationException) exception;

                    assertThat(ex.getErrors()).hasSize(1);

                    assertThat(ex.getErrors().get(0).getField())
                            .isEqualTo("artName");

                    assertThat(ex.getErrors().get(0).getRejectedValue())
                            .isEqualTo("BKS");

                    assertThat(ex.getErrors().get(0).getMessage())
                            .isEqualTo("ArtName existiert bereits.");
                });

        verify(repository).existsByNameIgnoreCase("BOOKS");
        verify(repository).existsByArtNameIgnoreCase("BKS");
        verify(repository, never()).save(any(ProductCategory.class));
    }

    @Test
    void addProductCategory_duplicateNameAndArtName_throwsTwoFieldErrors() {
        NewProductCategoryDto newDto =
                new NewProductCategoryDto("Books", "BKS");

        when(repository.existsByNameIgnoreCase("BOOKS"))
                .thenReturn(true);

        when(repository.existsByArtNameIgnoreCase("BKS"))
                .thenReturn(true);

        assertThatThrownBy(() -> service.addProductCategory(newDto))
                .isInstanceOf(FieldValidationException.class)
                .satisfies(exception -> {
                    FieldValidationException ex =
                            (FieldValidationException) exception;

                    assertThat(ex.getErrors()).hasSize(2);

                    assertThat(ex.getErrors())
                            .extracting(ValidationErrorDto::getField)
                            .containsExactlyInAnyOrder(
                                    "name",
                                    "artName"
                            );
                });

        verify(repository).existsByNameIgnoreCase("BOOKS");
        verify(repository).existsByArtNameIgnoreCase("BKS");
        verify(repository, never()).save(any(ProductCategory.class));
    }

    // ===== GET BY ID =====
    @Test
    void getProductCategoryById_success() {
        ProductCategory entity = new ProductCategory();
        ProductCategoryDto dto = new ProductCategoryDto();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(modelMapper.map(entity, ProductCategoryDto.class)).thenReturn(dto);

        ProductCategoryDto result = service.getProductCategoryById(1L);

        assertThat(result).isNotNull();
    }

    @Test
    void getProductCategoryById_notFound_throws() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProductCategoryById(1L))
                .isInstanceOf(RestApiException.class)
                .hasMessageContaining("Category not found");
    }

    // ===== DELETE =====
    @Test
    void deleteProductCategoryById_success() {
        ProductCategory entity = new ProductCategory();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        service.deleteProductCategoryById(1L);

        verify(repository).delete(entity);
        verify(repository).flush();
    }

    @Test
    void deleteProductCategoryById_dataIntegrityViolation_throws() {
        ProductCategory entity = new ProductCategory();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        doThrow(DataIntegrityViolationException.class).when(repository).delete(entity);

        assertThatThrownBy(() -> service.deleteProductCategoryById(1L))
                .isInstanceOf(RestApiException.class)
                .hasMessageContaining("Category cannot be deleted because it is used in other records");
    }

    // ===== UPDATE =====
    @Test
    void updateProductCategory_success() {
        NewProductCategoryDto newDto = new NewProductCategoryDto("Books", "BKS");

        ProductCategory entity = new ProductCategory();
        entity.setId(1L);

        ProductCategoryDto dto = new ProductCategoryDto();

        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.existsByNameIgnoreCaseAndIdNot("BOOKS", 1L)).thenReturn(false);
        when(repository.existsByArtNameIgnoreCaseAndIdNot("BKS", 1L)).thenReturn(false);
        when(repository.save(entity)).thenReturn(entity);
        when(modelMapper.map(entity, ProductCategoryDto.class)).thenReturn(dto);

        ProductCategoryDto result = service.updateProductCategory(1L, newDto);

        assertThat(result).isNotNull();
        verify(repository).existsByNameIgnoreCaseAndIdNot("BOOKS", 1L);
        verify(repository).existsByArtNameIgnoreCaseAndIdNot("BKS", 1L);
        verify(repository).save(entity);
    }

    @Test
    void updateProductCategory_duplicateName_throwsFieldValidationException() {
        NewProductCategoryDto newDto =
                new NewProductCategoryDto("Books", "BKS");

        ProductCategory entity = new ProductCategory();
        entity.setId(1L);

        when(repository.findById(1L))
                .thenReturn(Optional.of(entity));

        when(repository.existsByNameIgnoreCaseAndIdNot("BOOKS", 1L))
                .thenReturn(true);

        when(repository.existsByArtNameIgnoreCaseAndIdNot("BKS", 1L))
                .thenReturn(false);

        assertThatThrownBy(() -> service.updateProductCategory(1L, newDto))
                .isInstanceOf(FieldValidationException.class)
                .satisfies(exception -> {
                    FieldValidationException ex =
                            (FieldValidationException) exception;

                    assertThat(ex.getErrors()).hasSize(1);

                    assertThat(ex.getErrors().get(0).getField())
                            .isEqualTo("name");

                    assertThat(ex.getErrors().get(0).getRejectedValue())
                            .isEqualTo("BOOKS");

                    assertThat(ex.getErrors().get(0).getMessage())
                            .isEqualTo("Kategorie existiert bereits.");
                });

        verify(repository).findById(1L);
        verify(repository).existsByNameIgnoreCaseAndIdNot("BOOKS", 1L);
        verify(repository).existsByArtNameIgnoreCaseAndIdNot("BKS", 1L);
        verify(repository, never()).save(any(ProductCategory.class));
    }

    @Test
    void updateProductCategory_duplicateArtName_throwsFieldValidationException() {
        NewProductCategoryDto newDto =
                new NewProductCategoryDto("Books", "BKS");

        ProductCategory entity = new ProductCategory();
        entity.setId(1L);

        when(repository.findById(1L))
                .thenReturn(Optional.of(entity));

        when(repository.existsByNameIgnoreCaseAndIdNot("BOOKS", 1L))
                .thenReturn(false);

        when(repository.existsByArtNameIgnoreCaseAndIdNot("BKS", 1L))
                .thenReturn(true);

        assertThatThrownBy(() -> service.updateProductCategory(1L, newDto))
                .isInstanceOf(FieldValidationException.class)
                .satisfies(exception -> {
                    FieldValidationException ex =
                            (FieldValidationException) exception;

                    assertThat(ex.getErrors()).hasSize(1);

                    assertThat(ex.getErrors().get(0).getField())
                            .isEqualTo("artName");

                    assertThat(ex.getErrors().get(0).getRejectedValue())
                            .isEqualTo("BKS");

                    assertThat(ex.getErrors().get(0).getMessage())
                            .isEqualTo("ArtName existiert bereits.");
                });

        verify(repository).findById(1L);
        verify(repository).existsByNameIgnoreCaseAndIdNot("BOOKS", 1L);
        verify(repository).existsByArtNameIgnoreCaseAndIdNot("BKS", 1L);
        verify(repository, never()).save(any(ProductCategory.class));
    }

    @Test
    void updateProductCategory_duplicateNameAndArtName_throwsTwoFieldErrors() {
        NewProductCategoryDto newDto =
                new NewProductCategoryDto("Books", "BKS");

        ProductCategory entity = new ProductCategory();
        entity.setId(1L);

        when(repository.findById(1L))
                .thenReturn(Optional.of(entity));

        when(repository.existsByNameIgnoreCaseAndIdNot("BOOKS", 1L))
                .thenReturn(true);

        when(repository.existsByArtNameIgnoreCaseAndIdNot("BKS", 1L))
                .thenReturn(true);

        assertThatThrownBy(() -> service.updateProductCategory(1L, newDto))
                .isInstanceOf(FieldValidationException.class)
                .satisfies(exception -> {
                    FieldValidationException ex =
                            (FieldValidationException) exception;

                    assertThat(ex.getErrors()).hasSize(2);

                    assertThat(ex.getErrors())
                            .extracting(ValidationErrorDto::getField)
                            .containsExactlyInAnyOrder("name", "artName");
                });

        verify(repository).findById(1L);
        verify(repository).existsByNameIgnoreCaseAndIdNot("BOOKS", 1L);
        verify(repository).existsByArtNameIgnoreCaseAndIdNot("BKS", 1L);
        verify(repository, never()).save(any(ProductCategory.class));
    }

    // ===== FIND ALL =====
    @Test
    void findAllProductCategories_success() {
        ProductCategory entity = new ProductCategory();
        ProductCategoryDto dto = new ProductCategoryDto();

        when(repository.findAll(any(Sort.class))).thenReturn(List.of(entity));
        when(modelMapper.map(entity, ProductCategoryDto.class)).thenReturn(dto);

        List<ProductCategoryDto> result = service.findAllProductCategories();

        assertThat(result).hasSize(1);
    }

    @Test
    void findAllProductCategories_empty_returnsEmptyList() {
        when(repository.findAll(any(Sort.class))).thenReturn(Collections.emptyList());

        List<ProductCategoryDto> result = service.findAllProductCategories();

        assertThat(result).isEmpty();
    }
}
