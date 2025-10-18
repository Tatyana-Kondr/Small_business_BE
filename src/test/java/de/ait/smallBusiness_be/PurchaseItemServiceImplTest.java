package de.ait.smallBusiness_be;

import static org.mockito.Mockito.*;

import de.ait.smallBusiness_be.products.dao.ProductRepository;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.purchases.dao.PurchaseItemRepository;
import de.ait.smallBusiness_be.purchases.dao.PurchaseRepository;
import de.ait.smallBusiness_be.purchases.dto.NewPurchaseItemDto;
import de.ait.smallBusiness_be.purchases.dto.PurchaseItemDto;
import de.ait.smallBusiness_be.purchases.model.Purchase;
import de.ait.smallBusiness_be.purchases.model.PurchaseItem;
import de.ait.smallBusiness_be.purchases.services.PurchaseItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseItemServiceImplTest {

    @Mock
    private PurchaseItemRepository purchaseItemRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private PurchaseItemServiceImpl purchaseItemService;

    private Purchase purchase;
    private Product product;
    private PurchaseItem purchaseItem;
    private NewPurchaseItemDto newPurchaseItemDto;

    @BeforeEach
    void setUp() {
        // Создаём Purchase
        purchase = new Purchase();
        purchase.setId(1L);
        purchase.setPurchaseItems(new ArrayList<>());

        // Создаём Product
        product = new Product();
        product.setId(1L);
        product.setName("Product A");

        // Создаём PurchaseItem
        purchaseItem = new PurchaseItem();
        purchaseItem.setId(1L);
        purchaseItem.setPurchase(purchase);
        purchaseItem.setProduct(product);
        purchaseItem.setQuantity(BigDecimal.valueOf(2));
        purchaseItem.setUnitPrice(BigDecimal.valueOf(10));
        purchaseItem.setTaxPercentage(BigDecimal.valueOf(10));
        purchaseItem.setPosition(1);

        // DTO
        newPurchaseItemDto = new NewPurchaseItemDto();
        newPurchaseItemDto.setProductId(product.getId());
        newPurchaseItemDto.setProductName(product.getName());
        newPurchaseItemDto.setQuantity(BigDecimal.valueOf(2));
        newPurchaseItemDto.setUnitPrice(BigDecimal.valueOf(10));
        newPurchaseItemDto.setTaxPercentage(BigDecimal.valueOf(10));
        newPurchaseItemDto.setTotalPrice(BigDecimal.valueOf(20));
        newPurchaseItemDto.setTaxAmount(BigDecimal.valueOf(2));
        newPurchaseItemDto.setTotalAmount(BigDecimal.valueOf(22));
        newPurchaseItemDto.setPosition(1);

        // lenient stubbing для modelMapper
        lenient().when(modelMapper.map(Mockito.any(NewPurchaseItemDto.class), Mockito.eq(PurchaseItem.class)))
                .thenAnswer(invocation -> {
                    NewPurchaseItemDto dto = invocation.getArgument(0);
                    PurchaseItem item = new PurchaseItem();
                    item.setUnitPrice(dto.getUnitPrice());
                    item.setQuantity(dto.getQuantity());
                    item.setTaxPercentage(dto.getTaxPercentage());
                    return item;
                });

        lenient().when(modelMapper.map(Mockito.any(PurchaseItem.class), Mockito.eq(PurchaseItemDto.class)))
                .thenReturn(new PurchaseItemDto());
    }

    @Test
    void createPurchaseItem_success() {
        // подготовка данных
        purchase.getPurchaseItems().clear();

        when(purchaseRepository.findById(purchase.getId())).thenReturn(Optional.of(purchase));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(purchaseItemRepository.findMaxPositionByPurchaseId(purchase.getId())).thenReturn(0);

        // имитация сохранения PurchaseItem: добавляем в коллекцию
        when(purchaseItemRepository.save(any(PurchaseItem.class)))
                .thenAnswer(invocation -> {
                    PurchaseItem item = invocation.getArgument(0);
                    purchase.getPurchaseItems().add(item); // добавляем в purchase
                    return item;
                });

        // имитация сохранения Purchase
        when(purchaseRepository.save(any(Purchase.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // ModelMapper
        when(modelMapper.map(any(NewPurchaseItemDto.class), eq(PurchaseItem.class)))
                .thenAnswer(invocation -> {
                    NewPurchaseItemDto dto = invocation.getArgument(0);
                    PurchaseItem item = new PurchaseItem();
                    item.setUnitPrice(dto.getUnitPrice());
                    item.setQuantity(dto.getQuantity());
                    item.setTaxPercentage(dto.getTaxPercentage());
                    return item;
                });

        when(modelMapper.map(any(PurchaseItem.class), eq(PurchaseItemDto.class)))
                .thenAnswer(invocation -> new PurchaseItemDto());

        // вызов метода
        PurchaseItemDto dto = purchaseItemService.createPurchaseItem(newPurchaseItemDto, purchase.getId());

        assertNotNull(dto);
        assertEquals(1, purchase.getPurchaseItems().size()); // теперь должно быть 1
        assertEquals(1, purchase.getPurchaseItems().get(0).getPosition()); // позиция тоже 1

        verify(purchaseItemRepository).save(any(PurchaseItem.class));
        verify(purchaseRepository, atLeastOnce()).save(purchase);
    }

    @Test
    void createPurchaseItem_purchaseNotFound_throwsException() {
        when(purchaseRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> purchaseItemService.createPurchaseItem(newPurchaseItemDto, 999L));

        assertEquals("Purchase not found with ID: 999", ex.getMessage());
    }

    @Test
    void createPurchaseItem_productNotFound_throwsException() {
        // Setup: Purchase существует
        when(purchaseRepository.findById(purchase.getId())).thenReturn(Optional.of(purchase));

        // lenient stub для Product с любым аргументом
        lenient().when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Проверяем, что выбрасывается IllegalArgumentException
        assertThrows(IllegalArgumentException.class,
                () -> purchaseItemService.createPurchaseItem(newPurchaseItemDto, purchase.getId()));
    }


    @Test
    void getAllPurchaseItemsByPurchaseId_success() {
        purchase.getPurchaseItems().add(purchaseItem);

        when(purchaseRepository.existsById(purchase.getId())).thenReturn(true);
        when(purchaseItemRepository.findByPurchaseId(purchase.getId()))
                .thenReturn(Stream.of(purchaseItem));

        List<PurchaseItemDto> list = purchaseItemService.getAllPurchaseItemsByPurchaseId(purchase.getId());

        assertEquals(1, list.size());
        verify(purchaseItemRepository).findByPurchaseId(purchase.getId());
    }

    @Test
    void getPurchaseItemById_success() {
        when(purchaseItemRepository.findById(1L)).thenReturn(Optional.of(purchaseItem));

        PurchaseItemDto dto = purchaseItemService.getPurchaseItemById(1L);

        assertNotNull(dto);
    }

    @Test
    void updatePurchaseItem_success() {
        when(purchaseItemRepository.findById(1L)).thenReturn(Optional.of(purchaseItem));
        when(purchaseItemRepository.save(Mockito.any(PurchaseItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseItemDto dto = purchaseItemService.updatePurchaseItem(1L, newPurchaseItemDto);

        assertNotNull(dto);
        verify(purchaseItemRepository).save(purchaseItem);
        verify(purchaseRepository).save(purchase); // пересчет totals
    }

    @Test
    void deletePurchaseItem_success() {
        purchase.getPurchaseItems().add(purchaseItem);

        when(purchaseItemRepository.findById(1L)).thenReturn(Optional.of(purchaseItem));
        when(purchaseRepository.save(purchase)).thenReturn(purchase);

        purchaseItemService.deletePurchaseItem(1L);

        assertTrue(purchase.getPurchaseItems().isEmpty());
        verify(purchaseItemRepository).delete(purchaseItem);
        verify(purchaseRepository).save(purchase);
    }
}
