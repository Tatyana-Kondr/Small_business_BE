package de.ait.smallBusiness_be;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import de.ait.smallBusiness_be.productions.dao.ProductionItemRepository;
import de.ait.smallBusiness_be.productions.dao.ProductionRepository;
import de.ait.smallBusiness_be.productions.dto.NewProductionItemDto;
import de.ait.smallBusiness_be.productions.dto.ProductionItemDto;
import de.ait.smallBusiness_be.productions.model.Production;
import de.ait.smallBusiness_be.productions.model.ProductionItem;
import de.ait.smallBusiness_be.productions.services.ProductionItemServiceImpl;
import de.ait.smallBusiness_be.products.dao.ProductRepository;
import de.ait.smallBusiness_be.products.model.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.Optional;

class ProductionItemServiceImplTest {

    @InjectMocks
    private ProductionItemServiceImpl service;

    @Mock
    private ProductionRepository productionRepository;

    @Mock
    private ProductionItemRepository productionItemRepository;

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
    void createProductionItem_success() {
        NewProductionItemDto newDto = new NewProductionItemDto();
        newDto.setProductId(1L);
        newDto.setQuantity(BigDecimal.valueOf(2));
        newDto.setUnitPrice(BigDecimal.valueOf(10));
        newDto.setType("PRODUKTIONSMATERIAL");

        Product product = new Product();
        Production production = new Production();

        ProductionItem productionItem = new ProductionItem();
        productionItem.setUnitPrice(newDto.getUnitPrice());
        productionItem.setQuantity(newDto.getQuantity());

        ProductionItem savedProductionItem = new ProductionItem();
        savedProductionItem.setUnitPrice(newDto.getUnitPrice());
        savedProductionItem.setQuantity(newDto.getQuantity());
        savedProductionItem.setTotalPrice(newDto.getUnitPrice().multiply(newDto.getQuantity()));

        ProductionItemDto dto = new ProductionItemDto();

        when(productionRepository.findById(1L)).thenReturn(Optional.of(production));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(modelMapper.map(newDto, ProductionItem.class)).thenReturn(productionItem);
        when(productionItemRepository.save(productionItem)).thenReturn(savedProductionItem);
        when(modelMapper.map(savedProductionItem, ProductionItemDto.class)).thenReturn(dto);

        ProductionItemDto result = service.createProductionItem(newDto, 1L);

        assertThat(result).isNotNull();
        assertThat(productionItem.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(20));
    }


    @Test
    void createProductionItem_noProduction_throws() {
        NewProductionItemDto newDto = new NewProductionItemDto();
        newDto.setProductId(2L);

        assertThatThrownBy(() -> service.createProductionItem(newDto, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ProductionID must not be null");
    }

    // ===== GET BY ID =====
    @Test
    void getProductionItemById_success() {
        ProductionItem entity = new ProductionItem();
        ProductionItemDto dto = new ProductionItemDto();

        when(productionItemRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(modelMapper.map(entity, ProductionItemDto.class)).thenReturn(dto);

        ProductionItemDto result = service.getProductionItemById(1L);

        assertThat(result).isNotNull();
    }

    @Test
    void getProductionItemById_notFound_throws() {
        when(productionItemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProductionItemById(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ProductionItem not found");
    }

    // ===== UPDATE =====
    @Test
    void updateProductionItem_success() {
        NewProductionItemDto newDto = new NewProductionItemDto();
        newDto.setQuantity(BigDecimal.valueOf(3));
        newDto.setUnitPrice(BigDecimal.valueOf(20));

        ProductionItem existing = new ProductionItem();
        existing.setQuantity(BigDecimal.valueOf(2));
        existing.setUnitPrice(BigDecimal.valueOf(10));

        ProductionItemDto dto = new ProductionItemDto();

        when(productionItemRepository.findById(1L)).thenReturn(Optional.of(existing));
        doAnswer(invocation -> {
            NewProductionItemDto dtoArg = invocation.getArgument(0);
            ProductionItem entityArg = invocation.getArgument(1);
            entityArg.setUnitPrice(dtoArg.getUnitPrice());
            entityArg.setQuantity(dtoArg.getQuantity());
            return null;
        }).when(modelMapper).map(newDto, existing);

        when(productionItemRepository.save(existing)).thenReturn(existing);
        when(modelMapper.map(existing, ProductionItemDto.class)).thenReturn(dto);


        ProductionItemDto result = service.updateProductionItem(1L, newDto);

        assertThat(result).isNotNull();
        assertThat(existing.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(60.00));
        verify(productionItemRepository).save(existing);
    }

    @Test
    void updateProductionItem_notFound_throws() {
        NewProductionItemDto newDto = new NewProductionItemDto();
        when(productionItemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateProductionItem(1L, newDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ProductionItem not found");
    }

    // ===== DELETE =====
    @Test
    void deleteProductionItem_success() {
        Production production = new Production();
        ProductionItem item = new ProductionItem();
        item.setId(1L);
        item.setProduction(production);
        production.setProductionItems(new java.util.ArrayList<>());
        production.getProductionItems().add(item);

        when(productionItemRepository.findById(1L)).thenReturn(Optional.of(item));

        service.deleteProductionItem(1L);

        verify(productionItemRepository).delete(item);
        verify(productionRepository).save(production);
        assertThat(production.getProductionItems()).doesNotContain(item);
    }

    @Test
    void deleteProductionItem_notFound_throws() {
        when(productionItemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteProductionItem(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ProductionItem not found");
    }
}

