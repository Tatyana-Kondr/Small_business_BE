package de.ait.smallBusiness_be;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.customers.services.CustomerService;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.payments.dao.PaymentRepository;
import de.ait.smallBusiness_be.payments.model.Payment;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.products.service.ProductService;
import de.ait.smallBusiness_be.purchases.model.PaymentStatus;
import de.ait.smallBusiness_be.sales.dao.SaleRepository;
import de.ait.smallBusiness_be.sales.dao.ShippingRepository;
import de.ait.smallBusiness_be.sales.dto.NewSaleDto;
import de.ait.smallBusiness_be.sales.dto.NewSaleItemDto;
import de.ait.smallBusiness_be.sales.dto.SaleDto;
import de.ait.smallBusiness_be.sales.models.Sale;
import de.ait.smallBusiness_be.sales.models.SaleItem;
import de.ait.smallBusiness_be.sales.models.Shipping;
import de.ait.smallBusiness_be.sales.services.DocumentService;
import de.ait.smallBusiness_be.sales.services.ShippingService;
import de.ait.smallBusiness_be.sales.services.impl.SaleServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class SaleServiceImplTest {

    @Mock private SaleRepository saleRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private ShippingRepository shippingRepository;
    @Mock private CustomerService customerService;
    @Mock private ProductService productService;
    @Mock private ShippingService shippingService;
    @Mock private DocumentService invoiceService;
    @Mock private ModelMapper modelMapper;

    @InjectMocks
    private SaleServiceImpl saleService;

    private Sale sale;
    private Customer customer;
    private Shipping shipping;
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
        itemDto.setPosition(1);

        sale = new Sale();
        sale.setId(1L);
        sale.setCustomer(customer);
        sale.setShipping(shipping);
        sale.setTotalAmount(BigDecimal.valueOf(100));
        sale.setPaymentStatus(PaymentStatus.AUSSTEHEND);
        sale.setSaleItems(new ArrayList<>());

        newSaleDto = new NewSaleDto();
        newSaleDto.setCustomerId(customer.getId());
        newSaleDto.setShippingId(shipping.getId());
        newSaleDto.setSalesItems(List.of(itemDto));
        newSaleDto.setDefaultTax(BigDecimal.valueOf(10));
        newSaleDto.setDefaultDiscount(BigDecimal.valueOf(5));

        // ModelMapper mocks
        lenient().when(modelMapper.map(any(NewSaleDto.class), eq(Sale.class)))
                .thenAnswer(invocation -> {
                    Sale s = new Sale();
                    s.setSaleItems(new ArrayList<>());
                    return s;
                });

        lenient().when(modelMapper.map(any(Sale.class), eq(SaleDto.class)))
                .thenAnswer(invocation -> {
                    Sale sale = invocation.getArgument(0);
                    SaleDto dto = new SaleDto();
                    dto.setInvoiceNumber(sale.getInvoiceNumber());
                    dto.setDeliveryBill(sale.getDeliveryBill());
                    // если нужно, можно ещё скопировать SaleItems
                    return dto;
                });

        lenient().when(modelMapper.map(any(NewSaleItemDto.class), eq(SaleItem.class)))
                .thenAnswer(invocation -> {
                    NewSaleItemDto dto = invocation.getArgument(0);
                    SaleItem saleItem = new SaleItem();
                    saleItem.setProductName(dto.getProductName());
                    saleItem.setUnitPrice(dto.getUnitPrice());
                    saleItem.setQuantity(dto.getQuantity());
                    saleItem.setDiscount(dto.getDiscount());
                    saleItem.setTax(dto.getTax());
                    return saleItem;
                });
    }

    @Test
    void createSale_success() {
        when(customerService.getCustomerOrThrow(customer.getId())).thenReturn(customer);
        when(shippingRepository.findById(shipping.getId())).thenReturn(Optional.of(shipping));
        when(productService.getProductOrThrow(product.getId())).thenReturn(product);
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(invoiceService).generateInvoicePdf(any(Sale.class), anyString());
        doNothing().when(invoiceService).generateDeliveryBillPdf(any(Sale.class), anyString());

        SaleDto result = saleService.createSale(newSaleDto);

        assertNotNull(result);
        verify(saleRepository).save(any(Sale.class));
        verify(invoiceService).generateInvoicePdf(any(Sale.class), eq("invoices"));
        verify(invoiceService).generateDeliveryBillPdf(any(Sale.class), eq("delivery-bill"));
    }

    @Test
    void updateSale_success() {
        NewSaleDto updateDto = new NewSaleDto();
        updateDto.setCustomerId(customer.getId());
        updateDto.setShippingId(shipping.getId());
        updateDto.setInvoiceNumber("RE-2025-0002");
        updateDto.setSalesItems(List.of(itemDto));
        updateDto.setTypeOfOperation("VERKAUF");
        updateDto.setTermsOfPayment("ÜBERWEISUNG_7_TAGE");
        updateDto.setPaymentStatus("AUSSTEHEND");

        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));
        when(customerService.getCustomerOrThrow(customer.getId())).thenReturn(customer);
        when(shippingRepository.findById(shipping.getId())).thenReturn(Optional.of(shipping));
        when(saleRepository.save(any(Sale.class))).thenReturn(sale);
        doNothing().when(invoiceService).deleteInvoicePdf(any(Sale.class), anyString());
        doNothing().when(invoiceService).deleteDeliveryBillPdf(any(Sale.class), anyString());

        SaleDto result = saleService.updateSale(1L, updateDto);

        assertNotNull(result);
        verify(invoiceService).deleteInvoicePdf(sale, "invoices");
        verify(invoiceService).deleteDeliveryBillPdf(sale, "delivery-bill");
        verify(saleRepository).save(any(Sale.class));
    }

    @Test
    void updatePaymentStatus_fullPaid_setsBEZAHLT() {
        sale.setTotalAmount(BigDecimal.valueOf(100));
        Payment payment = new Payment();
        payment.setAmount(BigDecimal.valueOf(100));
        payment.setPaymentDate(LocalDate.now());

        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));
        when(paymentRepository.findBySaleId(1L)).thenReturn(List.of(payment));
        when(saleRepository.save(any(Sale.class))).thenReturn(sale);

        SaleDto result = saleService.updatePaymentStatus(1L);

        assertEquals(PaymentStatus.BEZAHLT, sale.getPaymentStatus());
        verify(saleRepository).save(any(Sale.class));
    }

    @Test
    void getAllSales_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("salesDate").descending());
        Page<Sale> salesPage = new PageImpl<>(List.of(sale));

        when(saleRepository.findAll(pageable)).thenReturn(salesPage);

        Page<SaleDto> result = saleService.getAllSales(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(saleRepository).findAll(pageable);
    }

    @Test
    void getAllSales_empty_throwsRestApiException() {
        Pageable pageable = PageRequest.of(0, 10);

        when(saleRepository.findAll(pageable)).thenReturn(Page.empty());

        assertThrows(RestApiException.class, () -> saleService.getAllSales(pageable));
    }

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

        when(saleRepository.filterSalesByFields(pageable,
                1L, 1L, "Customer A", "INV-1", BigDecimal.valueOf(100),
                "BEZAHLT", LocalDate.now().minusDays(1), LocalDate.now(), "query"))
                .thenReturn(salesPage);

        Page<SaleDto> result = saleService.getAllSalesByFilter(pageable,
                1L, 1L, "Customer A", "INV-1", BigDecimal.valueOf(100),
                "BEZAHLT", LocalDate.now().minusDays(1), LocalDate.now(), "query");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(saleRepository).filterSalesByFields(pageable,
                1L, 1L, "Customer A", "INV-1", BigDecimal.valueOf(100),
                "BEZAHLT", LocalDate.now().minusDays(1), LocalDate.now(), "query");
    }

    @Test
    void createSale_generatesInvoiceNumberAndDeliveryBill() {
        when(customerService.getCustomerOrThrow(customer.getId())).thenReturn(customer);
        when(shippingRepository.findById(shipping.getId())).thenReturn(Optional.of(shipping));
        when(productService.getProductOrThrow(itemDto.getProductId())).thenReturn(product);
        when(saleRepository.existsByInvoiceNumber(anyString())).thenReturn(false);
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SaleDto result = saleService.createSale(newSaleDto);

        assertNotNull(result);
        assertNotNull(result.getInvoiceNumber());
        assertNotNull(result.getDeliveryBill());
        verify(saleRepository).save(any(Sale.class));
        verify(invoiceService).generateInvoicePdf(any(Sale.class), eq("invoices"));
        verify(invoiceService).generateDeliveryBillPdf(any(Sale.class), eq("delivery-bill"));
    }

    @Test
    void createSale_throwsWhenCannotGenerateUniqueInvoiceNumber() {
        NewSaleDto newSaleDto = new NewSaleDto();
        newSaleDto.setCustomerId(customer.getId());
        newSaleDto.setShippingId(shipping.getId());
        newSaleDto.setSalesItems(List.of(itemDto));

        lenient().when(customerService.getCustomerOrThrow(customer.getId())).thenReturn(customer);
        lenient().when(shippingRepository.findById(shipping.getId())).thenReturn(Optional.of(shipping));
        lenient().when(productService.getProductOrThrow(itemDto.getProductId())).thenReturn(product);
        lenient().when(saleRepository.existsByInvoiceNumber(anyString()))
                .thenReturn(true, true, true, true, true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> saleService.createSale(newSaleDto));

        assertEquals("Failed to generate a unique invoice number", ex.getMessage());

        verify(saleRepository, times(5)).existsByInvoiceNumber(anyString());
    }

}
