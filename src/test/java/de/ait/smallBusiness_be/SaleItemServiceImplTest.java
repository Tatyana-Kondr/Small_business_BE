package de.ait.smallBusiness_be;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.products.service.ProductService;
import de.ait.smallBusiness_be.sales.dao.SaleItemRepository;
import de.ait.smallBusiness_be.sales.dao.SaleRepository;
import de.ait.smallBusiness_be.sales.dto.NewSaleItemDto;
import de.ait.smallBusiness_be.sales.dto.SaleItemDto;
import de.ait.smallBusiness_be.sales.models.Sale;
import de.ait.smallBusiness_be.sales.models.SaleItem;
import de.ait.smallBusiness_be.sales.services.SaleService;
import de.ait.smallBusiness_be.sales.services.impl.SaleItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


@ExtendWith(MockitoExtension.class)
class SaleItemServiceImplTest {

    @Mock private SaleItemRepository saleItemRepository;
    @Mock private SaleService saleService;
    @Mock private ProductService productService;
    @Mock private ModelMapper modelMapper;
    @Mock private SaleRepository saleRepository;

    @InjectMocks
    private SaleItemServiceImpl saleItemService;

    private Sale sale;
    private Product product;
    private SaleItem saleItem;
    private NewSaleItemDto newSaleItemDto;
    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("Customer A");

        sale = new Sale();
        sale.setId(1L);
        sale.setCustomer(customer);
        sale.setDefaultDiscount(BigDecimal.valueOf(10));
        sale.setDefaultTax(BigDecimal.valueOf(5));

        product = Product.builder()
                .id(1L)
                .name("Product A")
                .sellingPrice(BigDecimal.valueOf(100))
                .build();

        newSaleItemDto = new NewSaleItemDto();
        newSaleItemDto.setProductId(product.getId());
        newSaleItemDto.setQuantity(BigDecimal.valueOf(2));
        newSaleItemDto.setUnitPrice(BigDecimal.valueOf(50));
        newSaleItemDto.setDiscount(BigDecimal.valueOf(10));
        newSaleItemDto.setTax(BigDecimal.valueOf(5));

        saleItem = new SaleItem();
        saleItem.setId(1L);
        saleItem.setSale(sale);
        saleItem.setQuantity(BigDecimal.valueOf(2));
        saleItem.setUnitPrice(BigDecimal.valueOf(50));
        saleItem.setDiscount(null);
        saleItem.setTax(null);

        lenient().when(modelMapper.map(any(NewSaleItemDto.class), eq(SaleItem.class)))
                .thenAnswer(invocation -> {
                    NewSaleItemDto dto = invocation.getArgument(0);
                    SaleItem item = new SaleItem();
                    item.setUnitPrice(dto.getUnitPrice());
                    item.setQuantity(dto.getQuantity());
                    item.setDiscount(dto.getDiscount());
                    item.setTax(dto.getTax());
                    item.setProductName(product.getName());
                    return item;
                });

        lenient().when(modelMapper.map(any(SaleItem.class), eq(SaleItemDto.class)))
                .thenAnswer(invocation -> new SaleItemDto());
    }

    @Test
    void createSaleItem_success() {
        when(saleService.getSaleOrThrow(sale.getId())).thenReturn(sale);
        when(productService.getProductOrThrow(product.getId())).thenReturn(product);
        when(saleItemRepository.findMaxPositionBySaleId(sale.getId())).thenReturn(null);
        when(saleItemRepository.save(any(SaleItem.class))).thenAnswer(invocation -> {
            SaleItem item = invocation.getArgument(0);
            item.setId(1L);
            sale.getSaleItems().add(item);
            return item;
        });
        when(saleRepository.save(any(Sale.class))).thenReturn(sale);

        SaleItemDto result = saleItemService.createSaleItem(sale.getId(), newSaleItemDto);

        assertNotNull(result);
        assertEquals(1, sale.getSaleItems().size());
        SaleItem created = sale.getSaleItems().get(0);
        assertEquals(BigDecimal.valueOf(90).setScale(2), created.getTotalPrice()); // 50*2 - 10%
        assertEquals(BigDecimal.valueOf(4.5).setScale(2), created.getTaxAmount()); // 5% от 90
        assertEquals(BigDecimal.valueOf(94.5).setScale(2), created.getTotalAmount());
    }

    @Test
    void getAllSaleItemsBySaleId_success() {
        sale.getSaleItems().add(saleItem);
        when(saleService.checkIfSaleExistsById(sale.getId())).thenReturn(true);
        when(saleItemRepository.findAllBySaleIdOrderByPosition(sale.getId()))
                .thenReturn(sale.getSaleItems());

        List<SaleItemDto> items = saleItemService.getAllSaleItemsBySaleId(sale.getId());

        assertEquals(1, items.size());
    }

    @Test
    void getSaleItemById_success() {
        when(saleItemRepository.findById(1L)).thenReturn(Optional.of(saleItem));

        SaleItemDto result = saleItemService.getSaleItemById(sale.getId(), 1L);

        assertNotNull(result);
    }

    @Test
    void updateSaleItem_success() {
        // given
        when(saleItemRepository.findById(1L)).thenReturn(Optional.of(saleItem));
        when(saleItemRepository.save(any(SaleItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(saleRepository.save(any(Sale.class))).thenReturn(sale);

        NewSaleItemDto updateDto = new NewSaleItemDto();
        updateDto.setQuantity(BigDecimal.valueOf(3));   // 3 шт
        updateDto.setUnitPrice(BigDecimal.valueOf(50)); // цена 50
        updateDto.setDiscount(BigDecimal.valueOf(10));  // скидка 10%
        updateDto.setTax(BigDecimal.valueOf(5));        // налог 5%

        SaleItemDto result = saleItemService.updateSaleItem(sale.getId(), 1L, updateDto);

        assertNotNull(result);

        assertEquals(new BigDecimal("15.00"), saleItem.getDiscountAmount(), "Discount amount must be correct");
        assertEquals(new BigDecimal("135.00"), saleItem.getTotalPrice(), "Total price without tax must be correct");
        assertEquals(new BigDecimal("6.75"), saleItem.getTaxAmount(), "Tax amount must be correct");
        assertEquals(new BigDecimal("141.75"), saleItem.getTotalAmount(), "Total amount must be correct");
    }


    @Test
    void deleteSaleItem_success() {
        sale.getSaleItems().add(saleItem);
        when(saleItemRepository.findById(1L)).thenReturn(Optional.of(saleItem));
        when(saleRepository.save(any(Sale.class))).thenReturn(sale);

        saleItemService.deleteSaleItem(sale.getId(), 1L);

        assertTrue(sale.getSaleItems().isEmpty());
    }

    @Test
    void deleteSaleItem_reordersPositions() {
        SaleItem item2 = SaleItem.builder().position(2).sale(sale).build();
        sale.getSaleItems().addAll(List.of(saleItem, item2));
        when(saleItemRepository.findById(1L)).thenReturn(Optional.of(saleItem));
        when(saleRepository.save(any(Sale.class))).thenReturn(sale);

        saleItemService.deleteSaleItem(sale.getId(), 1L);

        assertEquals(1, sale.getSaleItems().get(0).getPosition());
    }

    @Test
    void getAllSaleItemsBySaleId_saleNotFound_throws() {
        when(saleService.checkIfSaleExistsById(sale.getId())).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> saleItemService.getAllSaleItemsBySaleId(sale.getId()));
    }
}
