package de.ait.smallBusiness_be;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;

import de.ait.smallBusiness_be.customers.dao.CustomerRepository;
import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.payments.dao.PaymentRepository;
import de.ait.smallBusiness_be.payments.model.Payment;
import de.ait.smallBusiness_be.products.dao.ProductRepository;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.purchases.dao.PurchaseRepository;
import de.ait.smallBusiness_be.purchases.dto.NewPurchaseDto;
import de.ait.smallBusiness_be.purchases.dto.NewPurchaseItemDto;
import de.ait.smallBusiness_be.purchases.dto.PurchaseDto;
import de.ait.smallBusiness_be.purchases.model.PaymentStatus;
import de.ait.smallBusiness_be.purchases.model.Purchase;
import de.ait.smallBusiness_be.purchases.model.PurchaseItem;
import de.ait.smallBusiness_be.purchases.model.TypeOfDocument;
import de.ait.smallBusiness_be.purchases.services.PurchaseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceImplTest {

    @Mock private PurchaseRepository purchaseRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private ProductRepository productRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private ModelMapper modelMapper;

    @InjectMocks
    private PurchaseServiceImpl purchaseService;

    private Customer customer;
    private Product product;
    private Purchase purchase;
    private NewPurchaseDto newPurchaseDto;
    private NewPurchaseItemDto itemDto;
    private TypeOfDocument document;

    @BeforeEach
    void setUp() {

        customer = new Customer();
        customer.setId(1L);
        customer.setName("Vendor A");

        product = new Product();
        product.setId(1L);
        product.setName("Product A");
        product.setPurchasingPrice(BigDecimal.valueOf(10));

        document = new TypeOfDocument();
        document.setId(1L);
        document.setName("Document A");

        itemDto = new NewPurchaseItemDto();
        itemDto.setProductId(product.getId());
        itemDto.setProductName("Product 1");
        itemDto.setQuantity(BigDecimal.valueOf(2));
        itemDto.setUnitPrice(BigDecimal.valueOf(10));
        itemDto.setTotalPrice(BigDecimal.valueOf(20));
        itemDto.setTaxPercentage(BigDecimal.valueOf(10));
        itemDto.setTaxAmount(BigDecimal.valueOf(2));
        itemDto.setTotalAmount(BigDecimal.valueOf(22));
        itemDto.setPosition(1);

        newPurchaseDto = new NewPurchaseDto();
        newPurchaseDto.setVendorId(customer.getId());
        newPurchaseDto.setPurchaseItems(List.of(itemDto));
        newPurchaseDto.setPurchasingDate(LocalDate.now());
        newPurchaseDto.setDocumentId(document.getId());
        newPurchaseDto.setType("EINKAUF");
        newPurchaseDto.setPaymentStatus("OFFEN");
        newPurchaseDto.setDocumentNumber("DOC-1");

        purchase = new Purchase();
        purchase.setId(1L);
        purchase.setVendor(customer);
        purchase.setPurchaseItems(new ArrayList<>());
        purchase.setTotal(BigDecimal.valueOf(100));
        purchase.setPaymentStatus(PaymentStatus.OFFEN);

        lenient().when(modelMapper.map(Mockito.any(NewPurchaseDto.class), Mockito.eq(Purchase.class)))
                .thenAnswer(invocation -> {
                    Purchase p = new Purchase();
                    p.setVendor(customer);
                    p.setPurchaseItems(new ArrayList<>());
                    return p;
                });

        lenient().when(modelMapper.map(Mockito.any(NewPurchaseItemDto.class), Mockito.eq(PurchaseItem.class)))
                .thenAnswer(invocation -> {
                    NewPurchaseItemDto dto = invocation.getArgument(0);
                    PurchaseItem item = new PurchaseItem();
                    item.setQuantity(dto.getQuantity());
                    item.setUnitPrice(dto.getUnitPrice());
                    item.setPosition(dto.getPosition());
                    return item;
                });

        lenient().when(modelMapper.map(Mockito.any(Purchase.class), Mockito.eq(PurchaseDto.class)))
                .thenReturn(new PurchaseDto());
    }

    @Test
    void createPurchase_success() {
        Mockito.when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        Mockito.when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        Mockito.when(purchaseRepository.save(Mockito.any(Purchase.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseDto result = purchaseService.createPurchase(newPurchaseDto);

        assertNotNull(result);
        Mockito.verify(purchaseRepository).save(Mockito.any(Purchase.class));
    }

    @Test
    void getAllPurchases_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Purchase> page = new PageImpl<>(List.of(purchase));

        Mockito.when(purchaseRepository.findAll(pageable)).thenReturn(page);

        Page<PurchaseDto> result = purchaseService.getAllPurchases(pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getPurchaseById_success() {
        Mockito.when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));

        PurchaseDto dto = purchaseService.getPurchaseById(1L);

        assertThat(dto).isNotNull();
    }

    @Test
    void updatePurchase_success() {
        Mockito.when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));
        Mockito.when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        Mockito.when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        Mockito.when(purchaseRepository.save(Mockito.any(Purchase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseDto dto = purchaseService.updatePurchase(1L, newPurchaseDto);

        assertThat(dto).isNotNull();
    }

    @Test
    void deletePurchase_success() {
        Mockito.when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));
        purchaseService.deletePurchase(1L);
        Mockito.verify(purchaseRepository).delete(purchase);
    }

    @Test
    void updatePaymentStatus_setsPaid() {
        Payment payment = new Payment();
        payment.setAmount(BigDecimal.valueOf(100));

        Mockito.when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));
        Mockito.when(paymentRepository.findByPurchaseId(1L)).thenReturn(List.of(payment));
        Mockito.when(purchaseRepository.save(Mockito.any(Purchase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseDto dto = purchaseService.updatePaymentStatus(1L);

        assertThat(dto).isNotNull();
        assertThat(purchase.getPaymentStatus()).isEqualTo(PaymentStatus.BEZAHLT);
    }

    @Test
    void searchPurchases_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Purchase> page = new PageImpl<>(List.of(purchase));

        Mockito.when(purchaseRepository.searchPurchases(pageable, "query")).thenReturn(page);

        Page<PurchaseDto> result = purchaseService.searchPurchases(pageable, "query");

        assertThat(result.getContent()).hasSize(1);
    }

}
