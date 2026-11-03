package de.ait.smallBusiness_be;

import de.ait.smallBusiness_be.customers.dao.CustomerRepository;
import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.payments.dao.PaymentRepository;
import de.ait.smallBusiness_be.payments.model.Payment;
import de.ait.smallBusiness_be.payments.model.PaymentType;
import de.ait.smallBusiness_be.products.dao.ProductRepository;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.purchases.dao.PurchaseDocumentRepository;
import de.ait.smallBusiness_be.purchases.dao.PurchaseItemRepository;
import de.ait.smallBusiness_be.purchases.dao.PurchaseRepository;
import de.ait.smallBusiness_be.purchases.dao.TypeOfDocumentRepository;
import de.ait.smallBusiness_be.purchases.dto.NewPurchaseDto;
import de.ait.smallBusiness_be.purchases.dto.NewPurchaseItemDto;
import de.ait.smallBusiness_be.purchases.dto.PurchaseDto;
import de.ait.smallBusiness_be.purchases.model.*;
import de.ait.smallBusiness_be.purchases.services.PendingFileDeletionService;
import de.ait.smallBusiness_be.purchases.services.PurchaseServiceImpl;
import de.ait.smallBusiness_be.warehouse.services.WarehouseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.never;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceImplTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private PurchaseItemRepository purchaseItemRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PurchaseDocumentRepository purchaseDocumentRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private TypeOfDocumentRepository typeOfDocumentRepository;

    @Mock
    private WarehouseService warehouseService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private PurchaseServiceImpl purchaseService;

    @Mock
    private PendingFileDeletionService pendingFileDeletionService;

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
        product.setSellingPrice(BigDecimal.valueOf(20));

        lenient()
                .when(purchaseItemRepository.findLatestPurchaseItemsByProductId(1L))
                .thenReturn(List.of());

        document = new TypeOfDocument();
        document.setId(1L);
        document.setName("Document A");

        itemDto = new NewPurchaseItemDto();
        itemDto.setProductId(product.getId());
        itemDto.setProductName("Product 1");
        itemDto.setQuantity(BigDecimal.valueOf(2));
        itemDto.setUnitPrice(BigDecimal.valueOf(50));
        itemDto.setDiscount(BigDecimal.valueOf(10));
        itemDto.setTaxPercentage(BigDecimal.valueOf(5));

        newPurchaseDto = new NewPurchaseDto();
        newPurchaseDto.setVendorId(customer.getId());
        newPurchaseDto.setPurchaseItems(List.of(itemDto));
        newPurchaseDto.setPurchasingDate(LocalDate.now());
        newPurchaseDto.setDocumentId(document.getId());
        newPurchaseDto.setType("EINKAUF");
        newPurchaseDto.setDocumentNumber("DOC-1");

        purchase = new Purchase();
        purchase.setId(1L);
        purchase.setVendor(customer);
        purchase.setDocument(document);
        purchase.setPurchaseItems(new ArrayList<>());
        purchase.setTotal(BigDecimal.valueOf(100));
        purchase.setPaymentStatus(PaymentStatus.OFFEN);

        /*
         * Пока createPurchase() всё ещё использует ModelMapper
         * для создания Purchase, этот mock оставляем.
         */
        lenient()
                .when(modelMapper.map(
                        Mockito.any(NewPurchaseDto.class),
                        Mockito.eq(Purchase.class)
                ))
                .thenAnswer(invocation -> {
                    Purchase p = new Purchase();
                    p.setPurchaseItems(new ArrayList<>());
                    return p;
                });

        lenient()
                .when(modelMapper.map(
                        Mockito.any(Purchase.class),
                        Mockito.eq(PurchaseDto.class)
                ))
                .thenReturn(new PurchaseDto());
    }

    @Test
    void createPurchase_success() {

        Mockito.when(customerRepository.findById(customer.getId()))
                .thenReturn(Optional.of(customer));

        Mockito.when(typeOfDocumentRepository.findById(document.getId()))
                .thenReturn(Optional.of(document));

        Mockito.when(productRepository.findById(product.getId()))
                .thenReturn(Optional.of(product));

        Mockito.when(purchaseRepository.saveAndFlush(Mockito.any(Purchase.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseDto result = purchaseService.createPurchase(newPurchaseDto);

        assertThat(result).isNotNull();

        ArgumentCaptor<Purchase> captor =
                ArgumentCaptor.forClass(Purchase.class);

        Mockito.verify(purchaseRepository)
                .saveAndFlush(captor.capture());

        Purchase savedPurchase = captor.getValue();

        assertThat(savedPurchase.getPurchaseItems()).hasSize(1);

        PurchaseItem item = savedPurchase.getPurchaseItems().get(0);

        assertThat(item.getProduct()).isEqualTo(product);
        assertThat(item.getProductName()).isEqualTo("Product 1");
        assertThat(item.getPosition()).isEqualTo(1);

        assertThat(item.getQuantity())
                .isEqualByComparingTo("2");

        assertThat(item.getUnitPrice())
                .isEqualByComparingTo("50");

        assertThat(item.getDiscount())
                .isEqualByComparingTo("10");

        assertThat(item.getDiscountAmount())
                .isEqualByComparingTo("10.00");

        assertThat(item.getTotalPrice())
                .isEqualByComparingTo("90.00");

        assertThat(item.getTaxPercentage())
                .isEqualByComparingTo("5");

        assertThat(item.getTaxAmount())
                .isEqualByComparingTo("4.50");

        assertThat(item.getTotalAmount())
                .isEqualByComparingTo("94.50");

        assertThat(savedPurchase.getSubtotal())
                .isEqualByComparingTo("90.00");

        assertThat(savedPurchase.getDiscountAmount())
                .isEqualByComparingTo("10.00");

        assertThat(savedPurchase.getTaxSum())
                .isEqualByComparingTo("4.50");

        assertThat(savedPurchase.getTotal())
                .isEqualByComparingTo("94.50");

        Mockito.verify(warehouseService)
                .recordOperation(
                        product,
                        savedPurchase.getType(),
                        item.getQuantity(),
                        savedPurchase.getId(),
                        customer,
                        savedPurchase.getPurchasingDate()
                );
    }

    @Test
    void getAllPurchases_success() {

        Pageable pageable = PageRequest.of(0, 10);
        Page<Purchase> page = new PageImpl<>(List.of(purchase));

        Mockito.when(purchaseRepository.findAllWithSorting(pageable))
                .thenReturn(page);

        Page<PurchaseDto> result =
                purchaseService.getAllPurchases(pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getPurchaseById_success() {

        Mockito.when(purchaseRepository.findById(1L))
                .thenReturn(Optional.of(purchase));

        PurchaseDto dto =
                purchaseService.getPurchaseById(1L);

        assertThat(dto).isNotNull();
    }

    @Test
    void updatePurchase_success() {

        Mockito.when(purchaseRepository.findById(1L))
                .thenReturn(Optional.of(purchase));

        Mockito.when(customerRepository.findById(customer.getId()))
                .thenReturn(Optional.of(customer));

        Mockito.when(typeOfDocumentRepository.findById(document.getId()))
                .thenReturn(Optional.of(document));

        Mockito.when(productRepository.findById(product.getId()))
                .thenReturn(Optional.of(product));

        Mockito.when(purchaseRepository.saveAndFlush(Mockito.any(Purchase.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseDto dto =
                purchaseService.updatePurchase(1L, newPurchaseDto);

        assertThat(dto).isNotNull();

        assertThat(purchase.getPurchaseItems()).hasSize(1);

        PurchaseItem item = purchase.getPurchaseItems().get(0);

        assertThat(item.getPosition()).isEqualTo(1);
        assertThat(item.getProductName()).isEqualTo("Product 1");

        assertThat(item.getDiscountAmount())
                .isEqualByComparingTo("10.00");

        assertThat(item.getTotalPrice())
                .isEqualByComparingTo("90.00");

        assertThat(item.getTaxAmount())
                .isEqualByComparingTo("4.50");

        assertThat(item.getTotalAmount())
                .isEqualByComparingTo("94.50");

        Mockito.verify(warehouseService)
                .syncDocument(
                        Mockito.eq(TypeOfOperation.EINKAUF),
                        Mockito.eq(1L),
                        Mockito.eq(customer),
                        Mockito.eq(newPurchaseDto.getPurchasingDate()),
                        Mockito.anyList()
                );
    }

    @Test
    void deletePurchase_success() {

        Mockito.when(purchaseRepository.findById(1L))
                .thenReturn(Optional.of(purchase));

        Mockito.when(paymentRepository.existsByPurchaseId(1L))
                .thenReturn(false);

        Mockito.when(purchaseDocumentRepository
                        .findAllByPurchaseIdOrderByIdAsc(1L))
                .thenReturn(List.of());

        purchaseService.deletePurchase(1L);

        Mockito.verify(warehouseService)
                .rollbackDocument(
                        Mockito.eq(1L),
                        Mockito.eq(List.of(
                                TypeOfOperation.EINKAUF,
                                TypeOfOperation.LIEFERANT_RABATT
                        ))
                );

        Mockito.verify(pendingFileDeletionService)
                .createTasks(1L, List.of());

        Mockito.verify(purchaseRepository).delete(purchase);
        Mockito.verify(purchaseRepository).flush();

        Mockito.verify(pendingFileDeletionService, never())
                .processPendingFiles();
    }

    @Test
    void deletePurchase_withDocuments_processesFilesAfterCommit() {

        PurchaseDocument document1 = new PurchaseDocument();
        document1.setFileUrl("document1.pdf");

        PurchaseDocument document2 = new PurchaseDocument();
        document2.setFileUrl("document2.pdf");

        List<String> expectedUrls = List.of(
                "document1.pdf",
                "document2.pdf"
        );

        Mockito.when(purchaseRepository.findById(1L))
                .thenReturn(Optional.of(purchase));

        Mockito.when(purchaseDocumentRepository
                        .findAllByPurchaseIdOrderByIdAsc(1L))
                .thenReturn(List.of(document1, document2));

        TransactionSynchronizationManager.initSynchronization();

        try {

            purchaseService.deletePurchase(1L);

            Mockito.verify(pendingFileDeletionService)
                    .createTasks(1L, expectedUrls);

            Mockito.verify(purchaseRepository).delete(purchase);
            Mockito.verify(purchaseRepository).flush();

            // До COMMIT файлы ещё не должны обрабатываться
            Mockito.verify(pendingFileDeletionService, never())
                    .processPendingFiles();

            // Имитируем успешный COMMIT
            for (TransactionSynchronization synchronization :
                    TransactionSynchronizationManager.getSynchronizations()) {

                synchronization.afterCommit();
            }

            // После COMMIT обработчик должен запуститься
            Mockito.verify(pendingFileDeletionService)
                    .processPendingFiles();

        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void deletePurchase_paidPurchase_throwsException() {

        purchase.setPaymentStatus(PaymentStatus.BEZAHLT);

        Mockito.when(purchaseRepository.findById(1L))
                .thenReturn(Optional.of(purchase));

        assertThatThrownBy(() -> purchaseService.deletePurchase(1L))
                .isInstanceOf(RestApiException.class);

        Mockito.verify(purchaseRepository, never())
                .delete(Mockito.any(Purchase.class));

        Mockito.verifyNoInteractions(
                purchaseDocumentRepository,
                pendingFileDeletionService,
                warehouseService
        );
    }

    @Test
    void deletePurchase_withPayments_throwsException() {

        purchase.setPaymentStatus(PaymentStatus.OFFEN);

        Mockito.when(purchaseRepository.findById(1L))
                .thenReturn(Optional.of(purchase));

        Mockito.when(paymentRepository.existsByPurchaseId(1L))
                .thenReturn(true);

        assertThatThrownBy(() -> purchaseService.deletePurchase(1L))
                .isInstanceOf(RestApiException.class);

        Mockito.verify(purchaseRepository, never())
                .delete(Mockito.any(Purchase.class));

        Mockito.verifyNoInteractions(
                purchaseDocumentRepository,
                pendingFileDeletionService,
                warehouseService
        );
    }

    @Test
    void updatePaymentStatus_setsPaid() {

        Payment payment = new Payment();
        payment.setType(PaymentType.AUSGABE);
        payment.setAmount(BigDecimal.valueOf(100));

        Mockito.when(purchaseRepository.findById(1L))
                .thenReturn(Optional.of(purchase));

        Mockito.when(paymentRepository.findByPurchaseId(1L))
                .thenReturn(List.of(payment));

        PurchaseDto dto =
                purchaseService.updatePaymentStatus(1L);

        assertThat(dto).isNotNull();

        assertThat(purchase.getPaymentStatus())
                .isEqualTo(PaymentStatus.BEZAHLT);
    }

    @Test
    void searchPurchases_success() {

        Pageable pageable = PageRequest.of(0, 10);
        Page<Purchase> page = new PageImpl<>(List.of(purchase));

        Mockito.when(purchaseRepository.searchPurchases(pageable, "query"))
                .thenReturn(page);

        Page<PurchaseDto> result =
                purchaseService.searchPurchases(pageable, "query");

        assertThat(result.getContent()).hasSize(1);
    }
}