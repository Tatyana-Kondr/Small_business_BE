package de.ait.smallBusiness_be;

import de.ait.smallBusiness_be.customers.dao.CustomerRepository;
import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.payments.dao.PaymentMethodRepository;
import de.ait.smallBusiness_be.payments.dao.PaymentProcessRepository;
import de.ait.smallBusiness_be.payments.dao.PaymentRepository;
import de.ait.smallBusiness_be.payments.dto.NewPaymentDto;
import de.ait.smallBusiness_be.payments.dto.PaymentDto;
import de.ait.smallBusiness_be.payments.dto.PaymentPrefillDto;
import de.ait.smallBusiness_be.payments.model.Payment;
import de.ait.smallBusiness_be.payments.model.PaymentMethod;
import de.ait.smallBusiness_be.payments.model.PaymentProcess;
import de.ait.smallBusiness_be.payments.model.PaymentType;
import de.ait.smallBusiness_be.payments.services.impl.PaymentServiceImpl;
import de.ait.smallBusiness_be.purchases.dao.PurchaseRepository;
import de.ait.smallBusiness_be.purchases.model.Purchase;
import de.ait.smallBusiness_be.purchases.services.PurchaseService;
import de.ait.smallBusiness_be.sales.dao.SaleRepository;
import de.ait.smallBusiness_be.sales.models.Sale;
import de.ait.smallBusiness_be.sales.services.SaleService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceImplTest {

    @InjectMocks
    private PaymentServiceImpl service;

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private SaleRepository saleRepository;
    @Mock
    private PurchaseRepository purchaseRepository;
    @Mock
    private PaymentMethodRepository paymentMethodRepository;
    @Mock
    private PaymentProcessRepository paymentProcessRepository;
    @Mock
    private PurchaseService purchaseService;
    @Mock
    private SaleService saleService;
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

    // ===== PREFILL FOR SALE =====
    @Test
    void getPrefillDataForSale_success() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("John");

        Sale sale = new Sale();
        sale.setId(1L);
        sale.setTotalAmount(BigDecimal.valueOf(100));
        sale.setCustomer(customer);
        sale.setInvoiceNumber("INV-001");

        Payment payment = new Payment();
        payment.setAmount(BigDecimal.valueOf(30));

        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));
        when(paymentRepository.findBySaleId(1L)).thenReturn(List.of(payment));

        PaymentPrefillDto result = service.getPrefillDataForSale(1L);

        assertThat(result).isNotNull();
        assertThat(result.getAmountLeft()).isEqualTo(BigDecimal.valueOf(70));
    }

    @Test
    void getPrefillDataForSale_notFound_throws() {
        when(saleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPrefillDataForSale(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Sale not found");
    }

    // ===== PREFILL FOR PURCHASE =====
    @Test
    void getPrefillDataForPurchase_success() {
        Customer vendor = new Customer();
        vendor.setId(2L);
        vendor.setName("Vendor1");

        Purchase purchase = new Purchase();
        purchase.setId(1L);
        purchase.setTotal(BigDecimal.valueOf(200));
        purchase.setVendor(vendor);
        purchase.setDocumentNumber("DOC-001");

        Payment payment = new Payment();
        payment.setAmount(BigDecimal.valueOf(50));

        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));
        when(paymentRepository.findByPurchaseId(1L)).thenReturn(List.of(payment));

        PaymentPrefillDto result = service.getPrefillDataForPurchase(1L);

        assertThat(result).isNotNull();
        assertThat(result.getAmountLeft()).isEqualTo(BigDecimal.valueOf(150));
    }

    @Test
    void getPrefillDataForPurchase_notFound_throws() {
        when(purchaseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPrefillDataForPurchase(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Purchase not found");
    }

    // ===== CREATE PAYMENT =====
    @Test
    void createPayment_success() {
        NewPaymentDto newDto = new NewPaymentDto();
        newDto.setCustomerId(1L);
        newDto.setPaymentMethodId(2L);
        newDto.setPaymentProcessId(3L);
        newDto.setAmount(BigDecimal.valueOf(100));
        newDto.setPaymentDate(LocalDate.now());
        newDto.setType(PaymentType.EINNAHME);
        newDto.setDocument("RECHNUNG");

        Customer customer = new Customer();
        PaymentMethod method = new PaymentMethod();
        PaymentProcess process = new PaymentProcess();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(paymentMethodRepository.findById(2L)).thenReturn(Optional.of(method));
        when(paymentProcessRepository.findById(3L)).thenReturn(Optional.of(process));

        Payment payment = new Payment();
        PaymentDto dto = new PaymentDto();

        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(modelMapper.map(payment, PaymentDto.class)).thenReturn(dto);

        PaymentDto result = service.createPayment(newDto);

        assertThat(result).isNotNull();
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void createPayment_customerNotFound_throws() {
        NewPaymentDto newDto = new NewPaymentDto();
        newDto.setCustomerId(1L);

        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createPayment(newDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Customer not found");
    }

    // ===== GET PAYMENTS =====
    @Test
    void getPayments_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Payment payment = new Payment();
        PaymentDto dto = new PaymentDto();
        Page<Payment> page = new PageImpl<>(List.of(payment));

        when(paymentRepository.findAll(pageable)).thenReturn(page);
        when(modelMapper.map(payment, PaymentDto.class)).thenReturn(dto);

        Page<PaymentDto> result = service.getPayments(pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getPayments_empty_throws() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Payment> page = Page.empty();

        when(paymentRepository.findAll(pageable)).thenReturn(page);

        assertThatThrownBy(() -> service.getPayments(pageable))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("List of payments is empty");
    }

    // ===== DELETE PAYMENT =====
    @Test
    void deletePayment_success_sale() {
        Payment payment = new Payment();
        Sale sale = new Sale();
        sale.setId(1L);
        payment.setSale(sale);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        service.deletePayment(1L);

        verify(paymentRepository).deleteById(1L);
        verify(saleService).updatePaymentStatus(1L);
    }

    @Test
    void deletePayment_success_purchase() {
        Payment payment = new Payment();
        Purchase purchase = new Purchase();
        purchase.setId(2L);
        payment.setPurchase(purchase);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        service.deletePayment(1L);

        verify(paymentRepository).deleteById(1L);
        verify(purchaseService).updatePaymentStatus(2L);
    }

    @Test
    void deletePayment_notFound_throws() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deletePayment(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Payment not found");
    }

    // ===== UPDATE PAYMENT =====
    @Test
    void updatePayment_success_sale() {
        NewPaymentDto newDto = new NewPaymentDto();
        newDto.setCustomerId(1L);
        newDto.setPaymentMethodId(2L);
        newDto.setPaymentProcessId(3L);
        newDto.setAmount(BigDecimal.valueOf(150));
        newDto.setPaymentDate(LocalDate.now());
        newDto.setType(PaymentType.EINNAHME);
        newDto.setDocument("RECHNUNG");
        newDto.setDocumentNumber("INV-123");
        newDto.setSaleId(10L);

        Payment existing = new Payment();
        existing.setId(5L);

        Customer customer = new Customer();
        PaymentMethod method = new PaymentMethod();
        PaymentProcess process = new PaymentProcess();
        Sale sale = new Sale();

        when(paymentRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(paymentMethodRepository.findById(2L)).thenReturn(Optional.of(method));
        when(paymentProcessRepository.findById(3L)).thenReturn(Optional.of(process));
        when(saleRepository.findById(10L)).thenReturn(Optional.of(sale));

        Payment saved = new Payment();
        PaymentDto dto = new PaymentDto();
        when(paymentRepository.save(existing)).thenReturn(saved);
        when(modelMapper.map(saved, PaymentDto.class)).thenReturn(dto);

        PaymentDto result = service.updatePayment(5L, newDto);

        assertThat(result).isNotNull();
        verify(paymentRepository).save(existing);
        assertThat(existing.getAmount()).isEqualTo(BigDecimal.valueOf(150));
        assertThat(existing.getSale()).isEqualTo(sale);
        assertThat(existing.getCustomer()).isEqualTo(customer);
        assertThat(existing.getPaymentMethod()).isEqualTo(method);
        assertThat(existing.getPaymentProcess()).isEqualTo(process);
    }

    // ===== UPDATE PAYMENT WITH PURCHASE =====
    @Test
    void updatePayment_success_purchase() {
        NewPaymentDto newDto = new NewPaymentDto();
        newDto.setCustomerId(1L);
        newDto.setPaymentMethodId(2L);
        newDto.setPaymentProcessId(3L);
        newDto.setAmount(BigDecimal.valueOf(200));
        newDto.setPaymentDate(LocalDate.now());
        newDto.setType(PaymentType.AUSGABE);
        newDto.setDocument("RECHNUNG");
        newDto.setDocumentNumber("PUR-123");
        newDto.setPurchaseId(20L);

        Payment existing = new Payment();
        existing.setId(6L);

        Customer customer = new Customer();
        PaymentMethod method = new PaymentMethod();
        PaymentProcess process = new PaymentProcess();
        Purchase purchase = new Purchase();

        when(paymentRepository.findById(6L)).thenReturn(Optional.of(existing));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(paymentMethodRepository.findById(2L)).thenReturn(Optional.of(method));
        when(paymentProcessRepository.findById(3L)).thenReturn(Optional.of(process));
        when(purchaseRepository.findById(20L)).thenReturn(Optional.of(purchase));

        Payment saved = new Payment();
        PaymentDto dto = new PaymentDto();
        when(paymentRepository.save(existing)).thenReturn(saved);
        when(modelMapper.map(saved, PaymentDto.class)).thenReturn(dto);

        PaymentDto result = service.updatePayment(6L, newDto);

        assertThat(result).isNotNull();
        verify(paymentRepository).save(existing);
        assertThat(existing.getAmount()).isEqualTo(BigDecimal.valueOf(200));
        assertThat(existing.getPurchase()).isEqualTo(purchase);
        assertThat(existing.getCustomer()).isEqualTo(customer);
    }

    // ===== PREFILL WITH ZERO PAYMENT LEFT =====
    @Test
    void getPrefillDataForSale_amountLeftZero() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Alice");

        Sale sale = new Sale();
        sale.setId(1L);
        sale.setTotalAmount(BigDecimal.valueOf(50));
        sale.setCustomer(customer);
        sale.setInvoiceNumber("INV-002");

        Payment payment = new Payment();
        payment.setAmount(BigDecimal.valueOf(50));

        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));
        when(paymentRepository.findBySaleId(1L)).thenReturn(List.of(payment));

        PaymentPrefillDto result = service.getPrefillDataForSale(1L);

        assertThat(result.getAmountLeft()).isEqualTo(BigDecimal.ZERO);
    }

    // ===== PREFILL FOR PURCHASE WITH ZERO LEFT =====
    @Test
    void getPrefillDataForPurchase_amountLeftZero() {
        Customer vendor = new Customer();
        vendor.setId(2L);
        vendor.setName("Vendor2");

        Purchase purchase = new Purchase();
        purchase.setId(1L);
        purchase.setTotal(BigDecimal.valueOf(100));
        purchase.setVendor(vendor);
        purchase.setDocumentNumber("DOC-002");

        Payment payment = new Payment();
        payment.setAmount(BigDecimal.valueOf(150));

        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));
        when(paymentRepository.findByPurchaseId(1L)).thenReturn(List.of(payment));

        PaymentPrefillDto result = service.getPrefillDataForPurchase(1L);

        // amountLeft не может быть отрицательным
        assertThat(result.getAmountLeft()).isEqualTo(BigDecimal.ZERO);
    }

}
