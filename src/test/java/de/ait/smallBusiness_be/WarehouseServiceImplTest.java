
package de.ait.smallBusiness_be;

import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.products.dao.ProductRepository;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.purchases.model.TypeOfOperation;
import de.ait.smallBusiness_be.warehouse.dao.WarehouseRecordRepository;
import de.ait.smallBusiness_be.warehouse.dao.WarehouseRepository;
import de.ait.smallBusiness_be.warehouse.dto.WarehouseRecordDto;
import de.ait.smallBusiness_be.warehouse.dto.WarehouseStockDto;
import de.ait.smallBusiness_be.warehouse.models.Warehouse;
import de.ait.smallBusiness_be.warehouse.models.WarehouseRecord;
import de.ait.smallBusiness_be.warehouse.services.WarehouseServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceImplTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private WarehouseRecordRepository recordRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private WarehouseServiceImpl service;

    private Product product;
    private Warehouse warehouse;
    private LocalDate date;

    @BeforeEach
    void setUp() {
        product = mock(Product.class);

        warehouse = Warehouse.builder()
                .product(product)
                .quantity(new BigDecimal("10"))
                .build();

        date = LocalDate.of(2026, 10, 28);
    }

    @Test
    void recordOperation_purchase_increasesStock() {
        when(warehouseRepository.findByProduct(product)).thenReturn(Optional.of(warehouse));

        service.recordOperation(product, TypeOfOperation.EINKAUF, new BigDecimal("5"), 1L, null, date);

        assertThat(warehouse.getQuantity()).isEqualByComparingTo("15");

        verify(recordRepository).save(any(WarehouseRecord.class));
        verify(warehouseRepository).save(warehouse);
    }

    @Test
    void recordOperation_sale_decreasesStock() {
        when(warehouseRepository.findByProduct(product)).thenReturn(Optional.of(warehouse));

        service.recordOperation(product, TypeOfOperation.VERKAUF, new BigDecimal("3"), 1L, null, date);

        assertThat(warehouse.getQuantity()).isEqualByComparingTo("7");

        verify(warehouseRepository).save(warehouse);
    }

    @Test
    void recordOperation_newWarehouse_created() {
        when(warehouseRepository.findByProduct(product)).thenReturn(Optional.empty());

        service.recordOperation(product, TypeOfOperation.EINKAUF, new BigDecimal("5"), 1L, null, date);

        ArgumentCaptor<Warehouse> captor = ArgumentCaptor.forClass(Warehouse.class);

        verify(warehouseRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantity()).isEqualByComparingTo("5");
    }

    @Test
    void recordOperation_nullProduct_throws() {
        RestApiException exception = assertThrows(RestApiException.class,
                () -> service.recordOperation(null, TypeOfOperation.EINKAUF, BigDecimal.ONE, 1L, null, date));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(recordRepository, warehouseRepository);
    }

    @Test
    void recordOperation_nullType_throws() {
        RestApiException exception = assertThrows(RestApiException.class,
                () -> service.recordOperation(product, null, BigDecimal.ONE, 1L, null, date));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void recordOperation_nullQuantity_throws() {
        RestApiException exception = assertThrows(RestApiException.class,
                () -> service.recordOperation(product, TypeOfOperation.EINKAUF, null, 1L, null, date));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void recordOperation_zeroQuantity_throws() {
        RestApiException exception = assertThrows(RestApiException.class,
                () -> service.recordOperation(product, TypeOfOperation.EINKAUF, BigDecimal.ZERO, 1L, null, date));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void recordOperation_negativeQuantity_throws() {
        RestApiException exception = assertThrows(RestApiException.class,
                () -> service.recordOperation(product, TypeOfOperation.EINKAUF, new BigDecimal("-5"), 1L, null, date));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void recordOperation_nullDocumentId_throws() {
        RestApiException exception = assertThrows(RestApiException.class,
                () -> service.recordOperation(product, TypeOfOperation.EINKAUF, BigDecimal.ONE, null, null, date));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void recordOperation_nullDate_throws() {
        RestApiException exception = assertThrows(RestApiException.class,
                () -> service.recordOperation(product, TypeOfOperation.EINKAUF, BigDecimal.ONE, 1L, null, null));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }


    @Test
    void getStock_success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findByProduct(product)).thenReturn(Optional.of(warehouse));

        WarehouseStockDto dto = mock(WarehouseStockDto.class);

        when(modelMapper.map(warehouse, WarehouseStockDto.class)).thenReturn(dto);

        WarehouseStockDto result = service.getStock(1L);

        assertThat(result).isSameAs(dto);
    }

    @Test
    void getStock_noWarehouse_returnsZero() {
        when(product.getId()).thenReturn(1L);
        when(product.getName()).thenReturn("Test Product");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findByProduct(product)).thenReturn(Optional.empty());

        WarehouseStockDto result = service.getStock(1L);

        assertThat(result.getQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getProductId()).isEqualTo(1L);
        assertThat(result.getProductName()).isEqualTo("Test Product");
    }

    @Test
    void getStock_productNotFound_throws() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        RestApiException exception = assertThrows(RestApiException.class, () -> service.getStock(1L));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getProductHistory_success() {
        Pageable pageable = PageRequest.of(0, 10);

        WarehouseRecord record = WarehouseRecord.builder()
                .product(product)
                .quantity(BigDecimal.ONE)
                .date(date)
                .typeOfOperation(TypeOfOperation.EINKAUF)
                .build();

        WarehouseRecordDto dto = mock(WarehouseRecordDto.class);

        when(recordRepository.findAllByProductIdOrderByDateDesc(1L, pageable)).thenReturn(new PageImpl<>(List.of(record)));

        when(modelMapper.map(record, WarehouseRecordDto.class)).thenReturn(dto);

        Page<WarehouseRecordDto> result = service.getProductHistory(1L, pageable);

        assertThat(result.getContent()).containsExactly(dto);
    }

    @Test
    void getProductHistory_empty_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);

        when(recordRepository.findAllByProductIdOrderByDateDesc(1L, pageable)).thenReturn(Page.empty());

        Page<WarehouseRecordDto> result = service.getProductHistory(1L, pageable);

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void getAllStocks_success() {
        Pageable pageable = PageRequest.of(0, 10);

        WarehouseStockDto dto = mock(WarehouseStockDto.class);

        when(warehouseRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(warehouse)));

        when(modelMapper.map(warehouse, WarehouseStockDto.class)).thenReturn(dto);

        Page<WarehouseStockDto> result = service.getAllStocks(pageable);

        assertThat(result.getContent()).containsExactly(dto);
    }

    @Test
    void getAllStocks_empty_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);

        when(warehouseRepository.findAll(pageable)).thenReturn(Page.empty());

        Page<WarehouseStockDto> result = service.getAllStocks(pageable);

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void syncDocument_emptyItems_success() {
        when(recordRepository.findAllByDocumentIdAndTypeOfOperationIn(eq(1L), anyCollection())).thenReturn(List.of());

        service.syncDocument(TypeOfOperation.EINKAUF, 1L, null, date, List.of());

        verify(recordRepository).findAllByDocumentIdAndTypeOfOperationIn(eq(1L), anyCollection());
        verify(recordRepository, never()).save(any(WarehouseRecord.class));
    }

    @Test
    void syncDocument_nullType_throws() {
        RestApiException exception = assertThrows(RestApiException.class,
                () -> service.syncDocument(null, 1L, null, date, List.of()));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void syncDocument_nullItems_throws() {
        RestApiException exception = assertThrows(RestApiException.class,
                () -> service.syncDocument(TypeOfOperation.EINKAUF, 1L, null, date, null));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void syncDocument_nullItem_throws() {
        when(recordRepository.findAllByDocumentIdAndTypeOfOperationIn(eq(1L), anyCollection())).thenReturn(List.of());

        List<Object> items = new java.util.ArrayList<>();
        items.add(null);

        RestApiException exception = assertThrows(RestApiException.class,
                () -> service.syncDocument(TypeOfOperation.EINKAUF, 1L, null, date, items));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void syncDocument_invalidItem_throws() {
        when(recordRepository.findAllByDocumentIdAndTypeOfOperationIn(eq(1L), anyCollection())).thenReturn(List.of());

        RestApiException exception = assertThrows(RestApiException.class,
                () -> service.syncDocument(TypeOfOperation.EINKAUF, 1L, null, date, List.of(new Object())));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void rollbackDocument_emptyRecords_success() {
        when(recordRepository.findAllByDocumentIdAndTypeOfOperationIn(eq(1L), anyCollection())).thenReturn(List.of());
        service.rollbackDocument(1L, List.of(TypeOfOperation.EINKAUF));
        verify(recordRepository, never()).deleteAll(anyCollection());
    }

    @Test
    void rollbackDocument_purchase_decreasesStock() {
        WarehouseRecord record = WarehouseRecord.builder()
                .product(product)
                .typeOfOperation(TypeOfOperation.EINKAUF)
                .quantity(new BigDecimal("5"))
                .build();

        when(recordRepository.findAllByDocumentIdAndTypeOfOperationIn(eq(1L), anyCollection())).thenReturn(List.of(record));
        when(warehouseRepository.findByProduct(product)).thenReturn(Optional.of(warehouse));

        service.rollbackDocument(1L, List.of(TypeOfOperation.EINKAUF));

        assertThat(warehouse.getQuantity()).isEqualByComparingTo("5");
        verify(recordRepository).deleteAll(List.of(record));
    }

    @Test
    void rollbackDocument_sale_increasesStock() {
        WarehouseRecord record = WarehouseRecord.builder()
                .product(product)
                .typeOfOperation(TypeOfOperation.VERKAUF)
                .quantity(new BigDecimal("3"))
                .build();

        when(recordRepository.findAllByDocumentIdAndTypeOfOperationIn(eq(1L), anyCollection())).thenReturn(List.of(record));
        when(warehouseRepository.findByProduct(product)).thenReturn(Optional.of(warehouse));

        service.rollbackDocument(1L, List.of(TypeOfOperation.VERKAUF));

        assertThat(warehouse.getQuantity()).isEqualByComparingTo("13");
    }

    @Test
    void rollbackDocument_nullDocumentId_throws() {
        RestApiException exception = assertThrows(RestApiException.class,
                () -> service.rollbackDocument(null, List.of(TypeOfOperation.EINKAUF)));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void rollbackDocument_emptyTypes_throws() {
        RestApiException exception = assertThrows(RestApiException.class, () -> service.rollbackDocument(1L, List.of()));
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void rollbackDocument_invalidRecord_throws() {
        WarehouseRecord record = WarehouseRecord.builder()
                .product(product)
                .typeOfOperation(TypeOfOperation.EINKAUF)
                .quantity(BigDecimal.ZERO)
                .build();

        when(recordRepository.findAllByDocumentIdAndTypeOfOperationIn(eq(1L), anyCollection())).thenReturn(List.of(record));

        RestApiException exception = assertThrows(RestApiException.class,
                () -> service.rollbackDocument(1L, List.of(TypeOfOperation.EINKAUF)));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        verify(recordRepository, never()).deleteAll(anyCollection());
    }

    @Test
    void rollbackDocument_warehouseNotFound_throws() {
        WarehouseRecord record = WarehouseRecord.builder()
                .product(product)
                .typeOfOperation(TypeOfOperation.EINKAUF)
                .quantity(BigDecimal.ONE)
                .build();

        when(recordRepository.findAllByDocumentIdAndTypeOfOperationIn(eq(1L), anyCollection())).thenReturn(List.of(record));
        when(warehouseRepository.findByProduct(product)).thenReturn(Optional.empty());

        RestApiException exception = assertThrows(RestApiException.class, () -> service.rollbackDocument(1L, List.of(TypeOfOperation.EINKAUF)));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        verify(recordRepository, never()).deleteAll(anyCollection());
    }
}

