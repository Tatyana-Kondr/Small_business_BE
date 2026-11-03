package de.ait.smallBusiness_be;

import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.productions.dao.ProductionRepository;
import de.ait.smallBusiness_be.productions.dto.NewProductionDto;
import de.ait.smallBusiness_be.productions.dto.NewProductionItemDto;
import de.ait.smallBusiness_be.productions.dto.ProductionDto;
import de.ait.smallBusiness_be.productions.model.Production;
import de.ait.smallBusiness_be.productions.model.ProductionItem;
import de.ait.smallBusiness_be.productions.services.ProductionServiceImpl;
import de.ait.smallBusiness_be.products.dao.ProductRepository;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.purchases.model.TypeOfOperation;
import de.ait.smallBusiness_be.warehouse.services.WarehouseService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductionServiceImplTest {

    @Mock private ProductionRepository productionRepository;
    @Mock private ProductRepository productRepository;
    @Mock private WarehouseService warehouseService;
    @Mock private ModelMapper modelMapper;

    @InjectMocks private ProductionServiceImpl service;

    private Product finishedProduct;
    private Product materialProduct;
    private LocalDate productionDate;

    @BeforeEach
    void setUp() {
        finishedProduct = new Product();
        finishedProduct.setId(1L);
        materialProduct = new Product();
        materialProduct.setId(2L);
        productionDate = LocalDate.now();
    }
    private NewProductionDto newDto() {
        NewProductionItemDto material = new NewProductionItemDto();
        material.setProductId(2L);
        material.setQuantity(new BigDecimal("5.000"));
        material.setUnitPrice(new BigDecimal("10.00"));

        NewProductionDto dto = new NewProductionDto();
        dto.setProductId(1L);
        dto.setDateOfProduction(productionDate);
        dto.setQuantity(new BigDecimal("3.000"));
        dto.setUnitPrice(new BigDecimal("50.00"));
        dto.setProductionItems(List.of(material));
        return dto;
    }

    private Production production(Long id) {
        Production production = new Production();
        production.setId(id);
        production.setProduct(finishedProduct);
        production.setDateOfProduction(productionDate);
        production.setQuantity(new BigDecimal("3.000"));
        production.setUnitPrice(new BigDecimal("50.00"));
        production.setAmount(new BigDecimal("150.00"));
        production.setProductionItems(new ArrayList<>());
        return production;
    }

    private void mockProductsAndMaterial(NewProductionDto dto) {
        when(productRepository.findById(1L)).thenReturn(Optional.of(finishedProduct));
        when(productRepository.findById(2L)).thenReturn(Optional.of(materialProduct));

        NewProductionItemDto itemDto = dto.getProductionItems().get(0);
        ProductionItem mappedItem = new ProductionItem();
        mappedItem.setQuantity(itemDto.getQuantity());
        mappedItem.setUnitPrice(itemDto.getUnitPrice());
        when(modelMapper.map(itemDto, ProductionItem.class)).thenReturn(mappedItem);
    }

    @Test
    void createProduction_success() {
        NewProductionDto dto = newDto();
        Production mappedProduction = production(null);
        ProductionDto response = new ProductionDto();

        mockProductsAndMaterial(dto);
        when(modelMapper.map(dto, Production.class)).thenReturn(mappedProduction);
        when(productionRepository.save(mappedProduction)).thenAnswer(invocation -> {
            mappedProduction.setId(10L);
            return mappedProduction;
        });
        when(modelMapper.map(mappedProduction, ProductionDto.class)).thenReturn(response);

        ProductionDto result = service.createProduction(dto);

        assertThat(result).isSameAs(response);
        assertThat(mappedProduction.getType()).isEqualTo(TypeOfOperation.PRODUKTION);
        assertThat(mappedProduction.getAmount()).isEqualByComparingTo("150.00");
        assertThat(mappedProduction.getProductionItems()).hasSize(1);

        ProductionItem material = mappedProduction.getProductionItems().get(0);
        assertThat(material.getProduction()).isSameAs(mappedProduction);
        assertThat(material.getProduct()).isSameAs(materialProduct);
        assertThat(material.getType()).isEqualTo(TypeOfOperation.PRODUKTIONSMATERIAL);
        assertThat(material.getTotalPrice()).isEqualByComparingTo("50.00");

        verify(productionRepository).save(mappedProduction);
        verify(warehouseService).recordOperation(
                eq(materialProduct), eq(TypeOfOperation.PRODUKTIONSMATERIAL),
                eq(new BigDecimal("5.000")), eq(10L),
                eq(null), eq(productionDate));
        verify(warehouseService).recordOperation(
                eq(finishedProduct), eq(TypeOfOperation.PRODUKTION),
                eq(new BigDecimal("3.000")), eq(10L),
                eq(null), eq(productionDate));
        verify(warehouseService, never()).rollbackDocument(any(), any());
    }

    @Test
    void createProduction_productNotFound_throws() {
        NewProductionDto dto = newDto();
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createProduction(dto)).isInstanceOf(EntityNotFoundException.class);
        verifyNoInteractions(productionRepository, warehouseService);
    }

    @Test
    void createProduction_amountNotGreaterThanMaterials_throws() {
        NewProductionDto dto = newDto();
        dto.setUnitPrice(new BigDecimal("10.00")); // 3 * 10 = 30 < 50
        Production mappedProduction = production(null);
        mappedProduction.setUnitPrice(dto.getUnitPrice());

        mockProductsAndMaterial(dto);
        when(modelMapper.map(dto, Production.class)).thenReturn(mappedProduction);

        assertThatThrownBy(() -> service.createProduction(dto)).isInstanceOf(RestApiException.class);
        verify(productionRepository, never()).save(any());
        verifyNoInteractions(warehouseService);
    }

    @Test
    void getAllProductions_success() {
        Production existing = production(1L);
        Pageable pageable = PageRequest.of(0, 10);
        ProductionDto mapped = new ProductionDto();
        when(productionRepository.findAllWithSorting(pageable))
                .thenReturn(new PageImpl<>(List.of(existing)));
        when(modelMapper.map(existing, ProductionDto.class)).thenReturn(mapped);

        Page<ProductionDto> result = service.getAllProductions(pageable);

        assertThat(result.getContent()).containsExactly(mapped);
        verify(productionRepository).findAllWithSorting(pageable);
    }

    @Test
    void getAllProductions_empty_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(productionRepository.findAllWithSorting(pageable)).thenReturn(Page.empty(pageable));

        Page<ProductionDto> result = service.getAllProductions(pageable);

        assertThat(result.isEmpty()).isTrue();
        verify(productionRepository).findAllWithSorting(pageable);
    }

    @Test
    void getProductionById_success() {
        Production existing = production(1L);
        ProductionDto mapped = new ProductionDto();
        when(productionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(modelMapper.map(existing, ProductionDto.class)).thenReturn(mapped);

        assertThat(service.getProductionById(1L)).isSameAs(mapped);
    }

    @Test
    void getProductionById_notFound_throws() {
        when(productionRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getProductionById(1L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateProduction_success() {
        NewProductionDto dto = newDto();
        Production existing = production(1L);
        ProductionDto response = new ProductionDto();

        when(productionRepository.findById(1L)).thenReturn(Optional.of(existing));
        mockProductsAndMaterial(dto);
        when(productionRepository.save(existing)).thenReturn(existing);
        when(modelMapper.map(existing, ProductionDto.class)).thenReturn(response);

        ProductionDto result = service.updateProduction(1L, dto);

        assertThat(result).isSameAs(response);
        assertThat(existing.getAmount()).isEqualByComparingTo("150.00");
        assertThat(existing.getProductionItems()).hasSize(1);
        assertThat(existing.getProductionItems().get(0).getTotalPrice()).isEqualByComparingTo("50.00");
        verify(productionRepository).save(existing);
        verify(warehouseService).rollbackDocument(
                eq(1L), eq(List.of(TypeOfOperation.PRODUKTION,
                        TypeOfOperation.PRODUKTIONSMATERIAL)));
        verify(warehouseService).recordOperation(
                eq(materialProduct), eq(TypeOfOperation.PRODUKTIONSMATERIAL),
                eq(new BigDecimal("5.000")), eq(1L), eq(null), eq(productionDate));
        verify(warehouseService).recordOperation(
                eq(finishedProduct), eq(TypeOfOperation.PRODUKTION),
                eq(new BigDecimal("3.000")), eq(1L), eq(null), eq(productionDate));
    }

    @Test
    void updateProduction_notFound_throws() {
        when(productionRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updateProduction(1L, newDto())).isInstanceOf(EntityNotFoundException.class);
        verifyNoInteractions(warehouseService);
    }

    @Test
    void updateProduction_invalidAmount_doesNotTouchWarehouse() {
        NewProductionDto dto = newDto();
        dto.setUnitPrice(new BigDecimal("10.00"));
        Production existing = production(1L);
        when(productionRepository.findById(1L)).thenReturn(Optional.of(existing));
        mockProductsAndMaterial(dto);

        assertThatThrownBy(() -> service.updateProduction(1L, dto)).isInstanceOf(RestApiException.class);
        verify(productionRepository, never()).save(any());
        verifyNoInteractions(warehouseService);
    }

    @Test
    void deleteProduction_success() {
        Production existing = production(1L);
        when(productionRepository.findById(1L)).thenReturn(Optional.of(existing));

        service.deleteProduction(1L);

        verify(warehouseService).rollbackDocument(
                eq(1L), eq(List.of(TypeOfOperation.PRODUKTION,
                        TypeOfOperation.PRODUKTIONSMATERIAL)));
        verify(productionRepository).delete(existing);
    }

    @Test
    void deleteProduction_notFound_throws() {
        when(productionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteProduction(1L)).isInstanceOf(EntityNotFoundException.class);
        verifyNoInteractions(warehouseService);
        verify(productionRepository, never()).delete(any());
    }

    @Test
    void searchProduction_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Production first = production(1L);
        Production second = production(2L);
        when(productionRepository.searchProduction(pageable, "query")).thenReturn(new PageImpl<>(List.of(first, second)));
        when(modelMapper.map(any(Production.class), eq(ProductionDto.class))).thenAnswer(invocation -> new ProductionDto());

        Page<ProductionDto> result = service.searchProduction(pageable, "query");

        assertThat(result.getContent()).hasSize(2);
        verify(productionRepository).searchProduction(pageable, "query");
    }

    @Test
    void getAllProductionsByFilter_success() {
        Pageable pageable = PageRequest.of(0, 10);
        LocalDate start = productionDate.minusDays(7);
        LocalDate end = productionDate;
        Production existing = production(1L);
        when(productionRepository.getAllProductionsByFilter(pageable, start, end, "query"))
                .thenReturn(new PageImpl<>(List.of(existing)));
        when(modelMapper.map(existing, ProductionDto.class)).thenReturn(new ProductionDto());

        Page<ProductionDto> result = service.getAllProductionsByFilter(pageable, start, end, "query");

        assertThat(result.getContent()).hasSize(1);
        verify(productionRepository).getAllProductionsByFilter(pageable, start, end, "query");
    }
}
