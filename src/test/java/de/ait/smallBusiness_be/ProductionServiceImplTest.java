package de.ait.smallBusiness_be;

import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.productions.dao.ProductionRepository;
import de.ait.smallBusiness_be.productions.dto.NewProductionDto;
import de.ait.smallBusiness_be.productions.dto.NewProductionItemDto;
import de.ait.smallBusiness_be.productions.dto.ProductionDto;
import de.ait.smallBusiness_be.productions.model.Production;
import de.ait.smallBusiness_be.productions.services.ProductionServiceImpl;
import de.ait.smallBusiness_be.products.dao.ProductRepository;
import de.ait.smallBusiness_be.products.model.Product;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductionServiceImplTest {

    @InjectMocks
    private ProductionServiceImpl service;

    @Mock
    private ProductionRepository productionRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ModelMapper modelMapper;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    // ===== CREATE =====
    @Test
    void createProduction_success() {
        NewProductionDto dto = new NewProductionDto();
        dto.setProductId(1L);
        dto.setQuantity(new BigDecimal("10"));
        dto.setUnitPrice(new BigDecimal("5"));
        dto.setProductionItems(Collections.emptyList());

        Product product = new Product();
        product.setId(1L);

        Production production = new Production();
        production.setProduct(product);
        production.setQuantity(dto.getQuantity());
        production.setUnitPrice(dto.getUnitPrice());
        production.setAmount(dto.getQuantity().multiply(dto.getUnitPrice()));

        Production savedProduction = new Production();
        savedProduction.setId(1L);

        ProductionDto productionDto = new ProductionDto();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(modelMapper.map(dto, Production.class)).thenReturn(production);
        when(productionRepository.save(production)).thenReturn(savedProduction);
        when(modelMapper.map(savedProduction, ProductionDto.class)).thenReturn(productionDto);

        ProductionDto result = service.createProduction(dto);

        assertThat(result).isNotNull();
        verify(productionRepository).save(production);
    }

    @Test
    void createProduction_productNotFound_throws() {
        NewProductionDto dto = new NewProductionDto();
        dto.setProductId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createProduction(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product not found");
    }

    // ===== GET ALL =====
    @Test
    void getAllProductions_success() {
        Production production = new Production();
        production.setId(1L);

        Page<Production> productionsPage = new PageImpl<>(List.of(production));

        when(productionRepository.findAll(any(Pageable.class))).thenReturn(productionsPage);
        when(modelMapper.map(any(Production.class), eq(ProductionDto.class))).thenReturn(new ProductionDto());

        Page<ProductionDto> result = service.getAllProductions(PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(productionRepository).findAll(any(Pageable.class));
    }


    @Test
    void getAllProductions_empty_throws() {
        Page<Production> emptyPage = new PageImpl<>(Collections.emptyList());

        when(productionRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        assertThatThrownBy(() -> service.getAllProductions(PageRequest.of(0, 10)))
                .isInstanceOf(RestApiException.class)
                .hasMessageContaining("List is empty");
    }

    // ===== GET BY ID =====
    @Test
    void getProductionById_success() {
        Production production = new Production();
        ProductionDto dto = new ProductionDto();

        when(productionRepository.findById(1L)).thenReturn(Optional.of(production));
        when(modelMapper.map(production, ProductionDto.class)).thenReturn(dto);

        ProductionDto result = service.getProductionById(1L);

        assertThat(result).isNotNull();
    }

    @Test
    void getProductionById_notFound_throws() {
        when(productionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProductionById(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Production not found");
    }

    // ===== UPDATE =====
    @Test
    void updateProduction_success() {
        // Подготовка исходных объектов
        Product mainProduct = new Product();
        mainProduct.setId(1L);

        Production existingProduction = new Production();
        existingProduction.setId(1L);
        existingProduction.setProduct(mainProduct);
        existingProduction.setProductionItems(new ArrayList<>());
        existingProduction.setUnitPrice(BigDecimal.valueOf(100));
        existingProduction.setQuantity(BigDecimal.valueOf(2));
        existingProduction.setAmount(BigDecimal.valueOf(200));

        // Мокирование репозиториев
        when(productionRepository.findById(1L)).thenReturn(Optional.of(existingProduction));

        Product itemProduct = new Product();
        itemProduct.setId(2L);
        when(productRepository.findById(2L)).thenReturn(Optional.of(itemProduct));
        when(productRepository.findById(1L)).thenReturn(Optional.of(mainProduct));

        // Создаём DTO с хотя бы одним productionItem
        NewProductionItemDto itemDto = new NewProductionItemDto();
        itemDto.setProductId(2L);
        itemDto.setType("PRODUKTIONSMATERIAL");
        itemDto.setQuantity(BigDecimal.valueOf(5));
        itemDto.setUnitPrice(BigDecimal.valueOf(10));

        NewProductionDto newDto = new NewProductionDto();
        newDto.setProductId(1L);
        newDto.setType("PRODUKTION"); // валидный тип
        newDto.setDateOfProduction(LocalDate.now());
        newDto.setQuantity(BigDecimal.valueOf(3));
        newDto.setUnitPrice(BigDecimal.valueOf(50));
        newDto.setProductionItems(List.of(itemDto));

        // Мокирование сохранения
        Production updatedProduction = new Production();
        updatedProduction.setId(1L);
        when(productionRepository.save(any(Production.class))).thenReturn(updatedProduction);
        when(modelMapper.map(updatedProduction, ProductionDto.class)).thenReturn(new ProductionDto());

        // Вызов метода
        ProductionDto result = service.updateProduction(1L, newDto);

        // Проверки
        assertThat(result).isNotNull();
        verify(productionRepository).save(any(Production.class));
    }


    @Test
    void updateProduction_notFound_throws() {
        NewProductionDto dto = new NewProductionDto();
        when(productionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateProduction(1L, dto))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ===== DELETE =====
    @Test
    void deleteProduction_success() {
        Production production = new Production();
        when(productionRepository.findById(1L)).thenReturn(Optional.of(production));

        service.deleteProduction(1L);

        verify(productionRepository).delete(production);
    }

    @Test
    void deleteProduction_notFound_throws() {
        when(productionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteProduction(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Production not found");
    }

    // ===== SEARCH =====
    @Test
    void searchProduction_success() {
        Production production1 = new Production();
        production1.setId(1L);

        Production production2 = new Production();
        production2.setId(2L);

        // Создаём страницу с 2 элементами
        Page<Production> productionPage = new PageImpl<>(List.of(production1, production2));

        when(productionRepository.searchProduction(any(Pageable.class), eq("query")))
                .thenReturn(productionPage);

        when(modelMapper.map(any(Production.class), eq(ProductionDto.class)))
                .thenAnswer(invocation -> new ProductionDto());

        Page<ProductionDto> result = service.searchProduction(PageRequest.of(0, 10), "query");

        // Проверка, что вернулась страница с 2 элементами
        assertThat(result.getContent()).hasSize(2);
    }


    @Test
    void getAllProductionsByFilter_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Production> page = new PageImpl<>(List.of(new Production()));

        when(productionRepository.getAllProductionsByFilter(pageable, LocalDate.now(), LocalDate.now(), "query"))
                .thenReturn(page);
        when(modelMapper.map(any(Production.class), eq(ProductionDto.class))).thenReturn(new ProductionDto());

        Page<ProductionDto> result = service.getAllProductionsByFilter(pageable, LocalDate.now(), LocalDate.now(), "query");

        assertThat(result.getContent()).hasSize(1);
    }
}
