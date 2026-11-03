package de.ait.smallBusiness_be;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.customers.services.CustomerService;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.payments.dao.PaymentRepository;
import de.ait.smallBusiness_be.payments.model.Payment;
import de.ait.smallBusiness_be.payments.model.PaymentType;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.products.service.ProductService;
import de.ait.smallBusiness_be.purchases.model.PaymentStatus;
import de.ait.smallBusiness_be.purchases.model.TypeOfOperation;
import de.ait.smallBusiness_be.sales.dao.SaleRepository;
import de.ait.smallBusiness_be.sales.dao.ShippingRepository;
import de.ait.smallBusiness_be.sales.dao.TermOfPaymentRepository;
import de.ait.smallBusiness_be.sales.dto.NewSaleDto;
import de.ait.smallBusiness_be.sales.dto.NewSaleItemDto;
import de.ait.smallBusiness_be.sales.dto.SaleDto;
import de.ait.smallBusiness_be.sales.models.Sale;
import de.ait.smallBusiness_be.sales.models.Shipping;
import de.ait.smallBusiness_be.sales.models.TermOfPayment;
import de.ait.smallBusiness_be.sales.services.DocumentService;
import de.ait.smallBusiness_be.sales.services.TermOfPaymentService;
import de.ait.smallBusiness_be.sales.services.impl.SaleServiceImpl;
import de.ait.smallBusiness_be.warehouse.services.WarehouseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.nio.file.Path;

import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class SaleServiceImplTest {

    @Mock
    private SaleRepository saleRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ShippingRepository shippingRepository;
    @Mock
    private TermOfPaymentService termOfPaymentService;
    @Mock
    private CustomerService customerService;
    @Mock
    private ProductService productService;
    @Mock
    private DocumentService invoiceService;
    @Mock
    private WarehouseService warehouseService;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private SaleServiceImpl saleService;

    private Sale sale;
    private Customer customer;
    private Shipping shipping;
    private TermOfPayment termOfPayment;
    private Product product;
    private NewSaleItemDto itemDto;
    private NewSaleDto newSaleDto;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("Customer A");

        shipping = new Shipping();
        shipping.setId(1L);
        shipping.setName("DHL");

        termOfPayment = new TermOfPayment();
        termOfPayment.setId(1L);
        termOfPayment.setName("Betrag in Bar");

        product = new Product();
        product.setId(1L);
        product.setName("Product A");
        product.setSellingPrice(BigDecimal.valueOf(100));

        itemDto = new NewSaleItemDto();
        itemDto.setProductId(product.getId());
        itemDto.setProductName(product.getName());
        itemDto.setQuantity(BigDecimal.valueOf(2));
        itemDto.setUnitPrice(BigDecimal.valueOf(50));
        itemDto.setDiscount(BigDecimal.valueOf(10));
        itemDto.setTax(BigDecimal.valueOf(5));


        sale = new Sale();
        sale.setId(1L);
        sale.setCustomer(customer);
        sale.setShipping(shipping);
        sale.setTermsOfPayment(termOfPayment);
        sale.setInvoiceNumber("RE2026001");
        sale.setDeliveryBill("LF2026001");
        sale.setTypeOfOperation(TypeOfOperation.VERKAUF);
        sale.setPaymentStatus(PaymentStatus.OFFEN);
        sale.setSalesDate(LocalDate.now());
        sale.setDeliveryDate(LocalDate.now());
        sale.setTotalAmount(BigDecimal.valueOf(100));
        sale.setSaleItems(new ArrayList<>());

        newSaleDto = new NewSaleDto();
        newSaleDto.setCustomerId(customer.getId());
        newSaleDto.setShippingId(shipping.getId());
        newSaleDto.setTermsOfPaymentId(termOfPayment.getId());
        newSaleDto.setTypeOfOperation(TypeOfOperation.VERKAUF);
        newSaleDto.setSalesDate(LocalDate.now());
        newSaleDto.setDeliveryDate(LocalDate.now());
        newSaleDto.setSalesItems(List.of(itemDto));

        lenient()
                .when(modelMapper.map(any(Sale.class), eq(SaleDto.class)))
                .thenAnswer(invocation -> {
                    Sale mappedSale = invocation.getArgument(0);

                    SaleDto dto = new SaleDto();
                    dto.setInvoiceNumber(mappedSale.getInvoiceNumber());
                    dto.setDeliveryBill(mappedSale.getDeliveryBill());
                    dto.setTotalAmount(mappedSale.getTotalAmount());

                    return dto;
                });
    }


    // ---------------------------------------
    // createSale
    // ---------------------------------------

    @Test
    void createSale_success() {
        Path invoiceTemp = Path.of("invoice-temp.pdf");
        Path deliveryTemp = Path.of("delivery-temp.pdf");

        ArgumentCaptor<Sale> saleCaptor = ArgumentCaptor.forClass(Sale.class);

        when(customerService.getCustomerOrThrow(customer.getId()))
                .thenReturn(customer);

        when(shippingRepository.findById(shipping.getId()))
                .thenReturn(Optional.of(shipping));

        when(termOfPaymentService.getTermOfPaymentOrThrow(termOfPayment.getId()))
                .thenReturn(termOfPayment);

        when(productService.getProductOrThrow(product.getId()))
                .thenReturn(product);

        when(saleRepository.findLastInvoiceSequenceForYear(anyInt()))
                .thenReturn(0);

        when(saleRepository.existsByInvoiceNumber(anyString()))
                .thenReturn(false);

        when(saleRepository.saveAndFlush(any(Sale.class)))
                .thenAnswer(invocation -> {
                    Sale savedSale = invocation.getArgument(0);
                    savedSale.setId(1L);
                    return savedSale;
                });

        when(invoiceService.generateInvoiceTempPdf(
                any(Sale.class),
                eq("invoices")
        )).thenReturn(invoiceTemp);

        when(invoiceService.generateDeliveryBillTempPdf(
                any(Sale.class),
                eq("delivery-bill")
        )).thenReturn(deliveryTemp);

        SaleDto result = saleService.createSale(newSaleDto);

        assertNotNull(result);
        assertNotNull(result.getInvoiceNumber());
        assertNotNull(result.getDeliveryBill());

        verify(saleRepository).saveAndFlush(saleCaptor.capture());

        Sale savedSale = saleCaptor.getValue();

        assertEquals(
                0,
                new BigDecimal("10.00")
                        .compareTo(savedSale.getDiscountAmount())
        );

        assertEquals(
                0,
                new BigDecimal("90.00")
                        .compareTo(savedSale.getTotalPrice())
        );

        assertEquals(
                0,
                new BigDecimal("4.50")
                        .compareTo(savedSale.getTaxAmount())
        );

        assertEquals(
                0,
                new BigDecimal("94.50")
                        .compareTo(savedSale.getTotalAmount())
        );

        assertEquals(1, savedSale.getSaleItems().size());

        var item = savedSale.getSaleItems().get(0);

        assertEquals(1, item.getPosition());
        assertEquals(product, item.getProduct());
        assertEquals(product.getName(), item.getProductName());

        assertEquals(
                0,
                new BigDecimal("2")
                        .compareTo(item.getQuantity())
        );

        assertEquals(
                0,
                new BigDecimal("50")
                        .compareTo(item.getUnitPrice())
        );

        assertEquals(
                0,
                new BigDecimal("10")
                        .compareTo(item.getDiscount())
        );

        assertEquals(
                0,
                new BigDecimal("10.00")
                        .compareTo(item.getDiscountAmount())
        );

        assertEquals(
                0,
                new BigDecimal("90.00")
                        .compareTo(item.getTotalPrice())
        );

        assertEquals(
                0,
                new BigDecimal("5")
                        .compareTo(item.getTax())
        );

        assertEquals(
                0,
                new BigDecimal("4.50")
                        .compareTo(item.getTaxAmount())
        );

        assertEquals(
                0,
                new BigDecimal("94.50")
                        .compareTo(item.getTotalAmount())
        );
        // --- Repository ---

        verify(saleRepository)
                .saveAndFlush(any(Sale.class));

        // --- PDF ---

        verify(invoiceService)
                .generateInvoiceTempPdf(
                        any(Sale.class),
                        eq("invoices")
                );

        verify(invoiceService)
                .generateDeliveryBillTempPdf(
                        any(Sale.class),
                        eq("delivery-bill")
                );

        verify(invoiceService)
                .replaceInvoicePdf(
                        any(Sale.class),
                        eq("invoices"),
                        eq(invoiceTemp)
                );

        verify(invoiceService)
                .replaceDeliveryBillPdf(
                        any(Sale.class),
                        eq("delivery-bill"),
                        eq(deliveryTemp)
                );

        // --- Warehouse ---

        verify(warehouseService)
                .recordOperation(
                        eq(product),
                        eq(TypeOfOperation.VERKAUF),
                        eq(itemDto.getQuantity()),
                        eq(1L),
                        eq(customer),
                        eq(newSaleDto.getSalesDate())
                );
    }

    @Test
    void createSale_generatesInvoiceNumberAndDeliveryBill() {
        Path invoiceTemp = Path.of("invoice-temp.pdf");
        Path deliveryTemp = Path.of("delivery-temp.pdf");

        when(customerService.getCustomerOrThrow(customer.getId()))
                .thenReturn(customer);

        when(shippingRepository.findById(shipping.getId()))
                .thenReturn(Optional.of(shipping));

        when(termOfPaymentService.getTermOfPaymentOrThrow(termOfPayment.getId()))
                .thenReturn(termOfPayment);

        when(productService.getProductOrThrow(itemDto.getProductId()))
                .thenReturn(product);

        when(saleRepository.findLastInvoiceSequenceForYear(anyInt()))
                .thenReturn(0);

        when(saleRepository.existsByInvoiceNumber(anyString()))
                .thenReturn(false);

        when(saleRepository.saveAndFlush(any(Sale.class)))
                .thenAnswer(invocation -> {
                    Sale savedSale = invocation.getArgument(0);
                    savedSale.setId(1L);
                    return savedSale;
                });

        when(invoiceService.generateInvoiceTempPdf(
                any(Sale.class),
                eq("invoices")
        )).thenReturn(invoiceTemp);

        when(invoiceService.generateDeliveryBillTempPdf(
                any(Sale.class),
                eq("delivery-bill")
        )).thenReturn(deliveryTemp);

        SaleDto result = saleService.createSale(newSaleDto);

        assertNotNull(result);
        assertNotNull(result.getInvoiceNumber());
        assertNotNull(result.getDeliveryBill());

        assertTrue(result.getInvoiceNumber().startsWith("RE"));
        assertTrue(result.getDeliveryBill().startsWith("LF"));

        verify(saleRepository).saveAndFlush(any(Sale.class));

        verify(invoiceService).replaceInvoicePdf(
                any(Sale.class),
                eq("invoices"),
                eq(invoiceTemp)
        );

        verify(invoiceService).replaceDeliveryBillPdf(
                any(Sale.class),
                eq("delivery-bill"),
                eq(deliveryTemp)
        );
    }

    @Test
    void createSale_throwsWhenCannotGenerateUniqueInvoiceNumber() {
        NewSaleDto dto = new NewSaleDto();

        dto.setCustomerId(customer.getId());
        dto.setShippingId(shipping.getId());
        dto.setTermsOfPaymentId(termOfPayment.getId());

        dto.setTypeOfOperation(TypeOfOperation.VERKAUF);

        dto.setSalesDate(LocalDate.now());
        dto.setDeliveryDate(LocalDate.now());

        dto.setSalesItems(List.of(itemDto));

        when(customerService.getCustomerOrThrow(customer.getId()))
                .thenReturn(customer);

        when(shippingRepository.findById(shipping.getId()))
                .thenReturn(Optional.of(shipping));

        when(termOfPaymentService.getTermOfPaymentOrThrow(termOfPayment.getId()))
                .thenReturn(termOfPayment);

        when(saleRepository.findLastInvoiceSequenceForYear(anyInt()))
                .thenReturn(0);

        when(saleRepository.existsByInvoiceNumber(anyString()))
                .thenReturn(true, true, true, true, true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> saleService.createSale(dto)
        );

        assertEquals(
                "Failed to generate a unique invoice number",
                exception.getMessage()
        );

        verify(saleRepository, times(5))
                .existsByInvoiceNumber(anyString());

        verify(saleRepository, never())
                .saveAndFlush(any(Sale.class));

        verifyNoInteractions(invoiceService);
    }

    // ---------------------------------------
    // updateSale
    // ---------------------------------------

    @Test
    void updateSale_success() {
        Path invoiceTemp = Path.of("invoice-update-temp.pdf");
        Path deliveryTemp = Path.of("delivery-update-temp.pdf");

        LocalDate existingPaymentDate = LocalDate.now().minusDays(2);
        sale.setPaymentDate(existingPaymentDate);

        Payment existingPayment = new Payment();
        existingPayment.setType(PaymentType.EINNAHME);
        existingPayment.setAmount(BigDecimal.valueOf(20));
        existingPayment.setPaymentDate(existingPaymentDate);

        NewSaleDto updateDto = new NewSaleDto();
        updateDto.setCustomerId(customer.getId());
        updateDto.setShippingId(shipping.getId());
        updateDto.setTermsOfPaymentId(termOfPayment.getId());

        updateDto.setInvoiceNumber("RE2026002");
        updateDto.setDeliveryBill("LF2026002");

        updateDto.setTypeOfOperation(TypeOfOperation.VERKAUF);

        updateDto.setSalesDate(LocalDate.now());
        updateDto.setDeliveryDate(LocalDate.now().plusDays(2));

        updateDto.setSalesItems(List.of(itemDto));

        sale.setSaleItems(new ArrayList<>());

        when(saleRepository.findById(1L))
                .thenReturn(Optional.of(sale));

        when(customerService.getCustomerOrThrow(customer.getId()))
                .thenReturn(customer);

        when(shippingRepository.findById(shipping.getId()))
                .thenReturn(Optional.of(shipping));

        when(termOfPaymentService.getTermOfPaymentOrThrow(termOfPayment.getId()))
                .thenReturn(termOfPayment);

        when(productService.getProductOrThrow(product.getId()))
                .thenReturn(product);

        when(saleRepository.saveAndFlush(any(Sale.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(paymentRepository.findBySaleId(1L))
                .thenReturn(List.of(existingPayment));

        when(invoiceService.generateInvoiceTempPdf(
                any(Sale.class),
                eq("invoices")
        )).thenReturn(invoiceTemp);

        when(invoiceService.generateDeliveryBillTempPdf(
                any(Sale.class),
                eq("delivery-bill")
        )).thenReturn(deliveryTemp);

        SaleDto result = saleService.updateSale(1L, updateDto);

        assertNotNull(result);

        // paymentDate не должна изменяться при updateSale
        assertEquals(existingPaymentDate, sale.getPaymentDate());

        assertEquals(TypeOfOperation.VERKAUF, sale.getTypeOfOperation());
        assertEquals(PaymentStatus.ANZAHLUNG, sale.getPaymentStatus());

        assertEquals(1, sale.getSaleItems().size());
        assertEquals(product, sale.getSaleItems().get(0).getProduct());

        var updatedItem = sale.getSaleItems().get(0);

        assertEquals(1, updatedItem.getPosition());

        assertEquals(
                0,
                new BigDecimal("10.00")
                        .compareTo(updatedItem.getDiscountAmount())
        );

        assertEquals(
                0,
                new BigDecimal("90.00")
                        .compareTo(updatedItem.getTotalPrice())
        );

        assertEquals(
                0,
                new BigDecimal("4.50")
                        .compareTo(updatedItem.getTaxAmount())
        );

        assertEquals(
                0,
                new BigDecimal("94.50")
                        .compareTo(updatedItem.getTotalAmount())
        );


// Sale aggregate totals

        assertEquals(
                0,
                new BigDecimal("10.00")
                        .compareTo(sale.getDiscountAmount())
        );

        assertEquals(
                0,
                new BigDecimal("90.00")
                        .compareTo(sale.getTotalPrice())
        );

        assertEquals(
                0,
                new BigDecimal("4.50")
                        .compareTo(sale.getTaxAmount())
        );

        assertEquals(
                0,
                new BigDecimal("94.50")
                        .compareTo(sale.getTotalAmount())
        );

        verify(saleRepository).saveAndFlush(sale);

        verify(invoiceService).generateInvoiceTempPdf(
                sale,
                "invoices"
        );

        verify(invoiceService).generateDeliveryBillTempPdf(
                sale,
                "delivery-bill"
        );

        verify(invoiceService).replaceInvoicePdf(
                sale,
                "invoices",
                invoiceTemp
        );

        verify(invoiceService).replaceDeliveryBillPdf(
                sale,
                "delivery-bill",
                deliveryTemp
        );

        verify(invoiceService, never())
                .deleteInvoicePdf(any(Sale.class), anyString());

        verify(invoiceService, never())
                .deleteDeliveryBillPdf(any(Sale.class), anyString());

        verify(warehouseService).syncDocument(
                eq(TypeOfOperation.VERKAUF),
                eq(1L),
                eq(customer),
                eq(updateDto.getSalesDate()),
                anyList()
        );
    }

    @Test
    void createSale_whenDeliveryPdfGenerationFails_deletesInvoiceTemp() {
        Path invoiceTemp = Path.of("invoice-temp.pdf");

        when(customerService.getCustomerOrThrow(customer.getId()))
                .thenReturn(customer);

        when(shippingRepository.findById(shipping.getId()))
                .thenReturn(Optional.of(shipping));

        when(termOfPaymentService.getTermOfPaymentOrThrow(termOfPayment.getId()))
                .thenReturn(termOfPayment);

        when(productService.getProductOrThrow(product.getId()))
                .thenReturn(product);

        when(saleRepository.findLastInvoiceSequenceForYear(anyInt()))
                .thenReturn(0);

        when(saleRepository.existsByInvoiceNumber(anyString()))
                .thenReturn(false);

        when(saleRepository.saveAndFlush(any(Sale.class)))
                .thenAnswer(invocation -> {
                    Sale savedSale = invocation.getArgument(0);
                    savedSale.setId(1L);
                    return savedSale;
                });

        when(invoiceService.generateInvoiceTempPdf(
                any(Sale.class),
                eq("invoices")
        )).thenReturn(invoiceTemp);

        when(invoiceService.generateDeliveryBillTempPdf(
                any(Sale.class),
                eq("delivery-bill")
        )).thenThrow(new RestApiException(
                "Delivery PDF generation failed",
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
        ));

        assertThrows(
                RestApiException.class,
                () -> saleService.createSale(newSaleDto)
        );

        verify(invoiceService).deleteTempFile(invoiceTemp);

        verify(invoiceService, never()).replaceInvoicePdf(
                any(),
                anyString(),
                any()
        );

        verify(invoiceService, never()).replaceDeliveryBillPdf(
                any(),
                anyString(),
                any()
        );

        verifyNoInteractions(warehouseService);
    }

    // ---------------------------------------
    // updatePaymentStatus
    // ---------------------------------------

    @Test
    void updatePaymentStatus_fullPaid_setsBEZAHLT_andPaymentDate() {

        sale.setTotalAmount(BigDecimal.valueOf(100));
        sale.setPaymentStatus(PaymentStatus.OFFEN);

        Payment payment = new Payment();
        payment.setType(PaymentType.EINNAHME);
        payment.setAmount(BigDecimal.valueOf(100));

        LocalDate payDate = LocalDate.now();
        payment.setPaymentDate(payDate);

        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));

        when(paymentRepository.findBySaleId(1L)).thenReturn(List.of(payment));

        SaleDto result = saleService.updatePaymentStatus(1L);

        assertNotNull(result);
        assertEquals(PaymentStatus.BEZAHLT, sale.getPaymentStatus());
        assertEquals(payDate, sale.getPaymentDate());

        verify(paymentRepository, times(2)).findBySaleId(1L);

        verify(saleRepository, never()).save(any(Sale.class));
    }

    @Test
    void updatePaymentStatus_noPayments_setsOFFEN() {

        sale.setTotalAmount(BigDecimal.valueOf(100));
        sale.setPaymentStatus(PaymentStatus.BEZAHLT);
        sale.setPaymentDate(LocalDate.now().minusDays(2));

        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));

        when(paymentRepository.findBySaleId(1L)).thenReturn(List.of());

        SaleDto result = saleService.updatePaymentStatus(1L);

        assertNotNull(result);

        assertEquals(PaymentStatus.OFFEN, sale.getPaymentStatus());
        assertNull(sale.getPaymentDate());

        verify(paymentRepository, times(2)).findBySaleId(1L);

        verify(saleRepository, never()).save(any(Sale.class));
    }

    // ---------------------------------------
    // getAllSales
    // ---------------------------------------

    @Test
    void getAllSales_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("salesDate").descending());

        Page<Sale> salesPage = new PageImpl<>(List.of(sale));

        when(saleRepository.findAllWithSorting(pageable)).thenReturn(salesPage);

        Page<SaleDto> result = saleService.getAllSales(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        verify(saleRepository).findAllWithSorting(pageable);
    }

    @Test
    void getAllSales_empty_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("salesDate").descending());

        when(saleRepository.findAllWithSorting(pageable)).thenReturn(Page.empty());

        Page<SaleDto> result = saleService.getAllSales(pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());

        verify(saleRepository).findAllWithSorting(pageable);
    }

    // ---------------------------------------
    // searchSales / filterSales
    // ---------------------------------------

    @Test
    void searchSales_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Sale> salesPage = new PageImpl<>(List.of(sale));

        when(saleRepository.searchSales(pageable, "query")).thenReturn(salesPage);

        Page<SaleDto> result = saleService.searchSales(pageable, "query");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(saleRepository).searchSales(pageable, "query");
    }

    @Test
    void getAllSalesByFilter_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Sale> salesPage = new PageImpl<>(List.of(sale));

        LocalDate start = LocalDate.now().minusDays(1);
        LocalDate end = LocalDate.now();

        when(saleRepository.filterSalesByFields(pageable,
                1L, 1L, "Customer A", "INV-1", BigDecimal.valueOf(100),
                "BEZAHLT", start, end, "query"))
                .thenReturn(salesPage);

        Page<SaleDto> result = saleService.getAllSalesByFilter(pageable,
                1L, 1L, "Customer A", "INV-1", BigDecimal.valueOf(100),
                "BEZAHLT", start, end, "query");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(saleRepository).filterSalesByFields(pageable,
                1L, 1L, "Customer A", "INV-1", BigDecimal.valueOf(100),
                "BEZAHLT", start, end, "query");
    }

    // ---------------------------------------
    // deleteSale
    // ---------------------------------------

    @Test
    void deleteSale_success() {

        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));

        saleService.deleteSale(1L);

        verify(invoiceService).deleteInvoicePdf(sale, "invoices");

        verify(invoiceService).deleteDeliveryBillPdf(sale, "delivery-bill");

        verify(warehouseService)
                .rollbackDocument(eq(1L), eq(List.of(TypeOfOperation.VERKAUF, TypeOfOperation.KUNDENERSTATTUNG)));

        verify(saleRepository).delete(sale);
    }

    @Test
    void checkIfSaleExistsById_returnsTrue() {
        when(saleRepository.existsById(1L)).thenReturn(true);

        boolean exists = saleService.checkIfSaleExistsById(1L);

        assertTrue(exists);
        verify(saleRepository).existsById(1L);
    }
}