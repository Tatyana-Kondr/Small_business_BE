package de.ait.smallBusiness_be;

import de.ait.smallBusiness_be.customers.dao.CustomerRepository;
import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.payments.dao.*;
import de.ait.smallBusiness_be.payments.dto.*;
import de.ait.smallBusiness_be.payments.model.*;
import de.ait.smallBusiness_be.payments.services.impl.PaymentServiceImpl;
import de.ait.smallBusiness_be.purchases.dao.*;
import de.ait.smallBusiness_be.purchases.model.*;
import de.ait.smallBusiness_be.purchases.services.PurchaseService;
import de.ait.smallBusiness_be.sales.dao.SaleRepository;
import de.ait.smallBusiness_be.sales.models.Sale;
import de.ait.smallBusiness_be.sales.services.SaleService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {
    @Mock PaymentRepository paymentRepository;
    @Mock CustomerRepository customerRepository;
    @Mock SaleRepository saleRepository;
    @Mock PurchaseRepository purchaseRepository;
    @Mock PaymentMethodRepository paymentMethodRepository;
    @Mock PaymentProcessRepository paymentProcessRepository;
    @Mock TypeOfDocumentRepository typeOfDocumentRepository;
    @Mock PurchaseService purchaseService;
    @Mock SaleService saleService;
    @Mock ModelMapper modelMapper;
    @InjectMocks PaymentServiceImpl service;

    private Customer customer;
    private PaymentMethod method;
    private PaymentProcess process;
    private TypeOfDocument document;
    private NewPaymentDto input;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("Muster GmbH");
        method = new PaymentMethod();
        method.setId(2L);
        process = new PaymentProcess();
        process.setId(3L);
        document = new TypeOfDocument();
        document.setId(4L);
        document.setName("RECHNUNG");
        input = new NewPaymentDto();
        input.setCustomerId(1L);
        input.setPaymentMethodId(2L);
        input.setPaymentProcessId(3L);
        input.setDocumentId(4L);
        input.setAmount(new BigDecimal("100.00"));
        input.setPaymentDate(LocalDate.of(2026, 10, 28));
        input.setType(PaymentType.EINNAHME);
        input.setDocumentNumber("RE2026001");
    }

    private void stubCommonReferences() {
        when(paymentMethodRepository.findById(2L)).thenReturn(Optional.of(method));
        when(paymentProcessRepository.findById(3L)).thenReturn(Optional.of(process));
        when(typeOfDocumentRepository.findById(4L)).thenReturn(Optional.of(document));
    }

    private Sale sale(long id, String total) {
        Sale sale = new Sale();
        sale.setId(id);
        sale.setCustomer(customer);
        sale.setTotalAmount(new BigDecimal(total));
        sale.setInvoiceNumber("RE2026001");
        return sale;
    }

    private Purchase purchase(long id, String total) {
        Purchase purchase = new Purchase();
        purchase.setId(id);
        purchase.setVendor(customer);
        purchase.setTotal(new BigDecimal(total));
        purchase.setDocument(document);
        purchase.setDocumentNumber("EK-001");
        return purchase;
    }

    private Payment payment(PaymentType type, String amount) {
        Payment p = new Payment();
        p.setType(type);
        p.setAmount(new BigDecimal(amount));
        return p;
    }

    @Test
    void salePrefill_partialPaymentAndRefund() {
        Sale sale = sale(10L, "100.00");
        when(saleRepository.findById(10L)).thenReturn(Optional.of(sale));
        when(paymentRepository.findBySaleId(10L)).thenReturn(List.of(
                payment(PaymentType.EINNAHME, "60.00"),
                payment(PaymentType.AUSGABE, "10.00")));
        when(typeOfDocumentRepository.findByName("RECHNUNG")).thenReturn(Optional.of(document));

        PaymentPrefillDto result = service.getPrefillDataForSale(10L);
        assertThat(result.getAlreadyPaid()).isEqualByComparingTo("50.00");
        assertThat(result.getAmountLeft()).isEqualByComparingTo("50.00");
        assertThat(result.getOverpayment()).isEqualByComparingTo("0");
        assertThat(result.getType()).isEqualTo(PaymentType.EINNAHME);
        assertThat(result.getCustomerId()).isEqualTo(1L);
        assertThat(result.getDocumentId()).isEqualTo(4L);
    }

    @Test
    void salePrefill_overpayment() {
        when(saleRepository.findById(10L)).thenReturn(Optional.of(sale(10L, "100.00")));
        when(paymentRepository.findBySaleId(10L)).thenReturn(List.of(payment(PaymentType.EINNAHME, "125.00")));
        when(typeOfDocumentRepository.findByName("RECHNUNG")).thenReturn(Optional.of(document));
        PaymentPrefillDto result = service.getPrefillDataForSale(10L);
        assertThat(result.getAmountLeft()).isEqualByComparingTo("0");
        assertThat(result.getOverpayment()).isEqualByComparingTo("25.00");
    }

    @Test
    void salePrefill_saleNotFound() {
        when(saleRepository.findById(10L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getPrefillDataForSale(10L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void salePrefill_documentNotFound() {
        when(saleRepository.findById(10L)).thenReturn(Optional.of(sale(10L, "100.00")));
        when(paymentRepository.findBySaleId(10L)).thenReturn(List.of());
        when(typeOfDocumentRepository.findByName("RECHNUNG")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getPrefillDataForSale(10L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void purchasePrefill_partialPaymentAndRefund() {
        when(purchaseRepository.findById(20L)).thenReturn(Optional.of(purchase(20L, "200.00")));
        when(paymentRepository.findByPurchaseId(20L)).thenReturn(List.of(
                payment(PaymentType.AUSGABE, "80.00"),
                payment(PaymentType.EINNAHME, "20.00")));
        PaymentPrefillDto result = service.getPrefillDataForPurchase(20L);
        assertThat(result.getAlreadyPaid()).isEqualByComparingTo("60.00");
        assertThat(result.getAmountLeft()).isEqualByComparingTo("140.00");
        assertThat(result.getType()).isEqualTo(PaymentType.AUSGABE);
        assertThat(result.getPurchaseId()).isEqualTo(20L);
    }

    @Test
    void purchasePrefill_overpayment() {
        when(purchaseRepository.findById(20L)).thenReturn(Optional.of(purchase(20L, "100.00")));
        when(paymentRepository.findByPurchaseId(20L)).thenReturn(List.of(payment(PaymentType.AUSGABE, "150.00")));
        PaymentPrefillDto result = service.getPrefillDataForPurchase(20L);
        assertThat(result.getAmountLeft()).isEqualByComparingTo("0");
        assertThat(result.getOverpayment()).isEqualByComparingTo("50.00");
    }

    @Test
    void purchasePrefill_notFound() {
        when(purchaseRepository.findById(20L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getPrefillDataForPurchase(20L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createPayment_standalone() {
        stubCommonReferences();
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(paymentRepository.saveAndFlush(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        PaymentDto expected = new PaymentDto();
        when(modelMapper.map(any(Payment.class), eq(PaymentDto.class))).thenReturn(expected);

        PaymentDto result = service.createPayment(input);
        assertThat(result).isSameAs(expected);
        org.mockito.ArgumentCaptor<Payment> captor = org.mockito.ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getCustomer()).isSameAs(customer);
        assertThat(captor.getValue().getPaymentMethod()).isSameAs(method);
        assertThat(captor.getValue().getPaymentProcess()).isSameAs(process);
        assertThat(captor.getValue().getDocument()).isSameAs(document);
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo("100.00");
        verifyNoInteractions(saleService, purchaseService);
    }

    @Test
    void createPayment_forSale_usesSaleCustomerAndUpdatesStatus() {
        input.setSaleId(10L);
        Sale sale = sale(10L, "100.00");
        stubCommonReferences();
        when(saleRepository.findById(10L)).thenReturn(Optional.of(sale));
        when(paymentRepository.saveAndFlush(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(modelMapper.map(any(Payment.class), eq(PaymentDto.class))).thenReturn(new PaymentDto());
        service.createPayment(input);
        verify(customerRepository, never()).findById(anyLong());
        verify(saleService).updatePaymentStatus(10L);
        verifyNoInteractions(purchaseService);
    }

    @Test
    void createPayment_forPurchase_usesVendorAndUpdatesStatus() {
        input.setPurchaseId(20L);
        stubCommonReferences();
        when(purchaseRepository.findById(20L)).thenReturn(Optional.of(purchase(20L, "100.00")));
        when(paymentRepository.saveAndFlush(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(modelMapper.map(any(Payment.class), eq(PaymentDto.class))).thenReturn(new PaymentDto());
        service.createPayment(input);
        verify(customerRepository, never()).findById(anyLong());
        verify(purchaseService).updatePaymentStatus(20L);
        verifyNoInteractions(saleService);
    }

    @Test
    void createPayment_bothReferences_throwsConflict() {
        input.setSaleId(10L);
        input.setPurchaseId(20L);
        RestApiException ex = catchThrowableOfType(() -> service.createPayment(input), RestApiException.class);
        assertThat(ex).isNotNull();
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        verifyNoInteractions(paymentRepository, saleRepository, purchaseRepository);
    }

    @Test
    void createPayment_missingMethod_throws() {
        when(paymentMethodRepository.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.createPayment(input)).isInstanceOf(EntityNotFoundException.class);
        verify(paymentRepository, never()).saveAndFlush(any(Payment.class));
    }

    @Test
    void createPayment_missingCustomer_throws() {
        stubCommonReferences();
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.createPayment(input)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getPayments_usesSortingRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Payment p = payment(PaymentType.EINNAHME, "20");
        when(paymentRepository.findAllWithSorting(pageable)).thenReturn(new PageImpl<>(List.of(p)));
        PaymentDto dto = new PaymentDto();
        when(modelMapper.map(p, PaymentDto.class)).thenReturn(dto);
        assertThat(service.getPayments(pageable).getContent()).containsExactly(dto);
    }

    @Test
    void getPayments_emptyPageIsValid() {
        Pageable pageable = PageRequest.of(0, 10);
        when(paymentRepository.findAllWithSorting(pageable)).thenReturn(Page.empty(pageable));
        assertThat(service.getPayments(pageable).getContent()).isEmpty();
    }

    @Test
    void getPayment_byId() {
        Payment p = payment(PaymentType.EINNAHME, "20");
        PaymentDto dto = new PaymentDto();
        when(paymentRepository.findById(5L)).thenReturn(Optional.of(p));
        when(modelMapper.map(p, PaymentDto.class)).thenReturn(dto);
        assertThat(service.getPayment(5L)).isSameAs(dto);
    }

    @Test
    void getPayment_notFound() {
        when(paymentRepository.findById(5L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getPayment(5L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updatePayment_sameSale_updatesStatus() {
        input.setSaleId(10L);
        Sale sale = sale(10L, "100");
        Payment existing = payment(PaymentType.EINNAHME, "30");
        existing.setSale(sale);
        when(paymentRepository.findById(5L)).thenReturn(Optional.of(existing));
        stubCommonReferences();
        when(saleRepository.findById(10L)).thenReturn(Optional.of(sale));
        when(paymentRepository.saveAndFlush(existing)).thenReturn(existing);
        when(modelMapper.map(existing, PaymentDto.class)).thenReturn(new PaymentDto());
        service.updatePayment(5L, input);
        verify(saleService, times(1)).updatePaymentStatus(10L);
        assertThat(existing.getAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    void updatePayment_movesFromSaleToPurchase_updatesBoth() {
        input.setPurchaseId(20L);
        Sale oldSale = sale(10L, "100");
        Purchase newPurchase = purchase(20L, "100");
        Payment existing = payment(PaymentType.EINNAHME, "30");
        existing.setSale(oldSale);
        when(paymentRepository.findById(5L)).thenReturn(Optional.of(existing));
        stubCommonReferences();
        when(purchaseRepository.findById(20L)).thenReturn(Optional.of(newPurchase));
        when(paymentRepository.saveAndFlush(existing)).thenReturn(existing);
        when(modelMapper.map(existing, PaymentDto.class)).thenReturn(new PaymentDto());
        service.updatePayment(5L, input);
        verify(saleService).updatePaymentStatus(10L);
        verify(purchaseService).updatePaymentStatus(20L);
        assertThat(existing.getSale()).isNull();
        assertThat(existing.getPurchase()).isSameAs(newPurchase);
        assertThat(existing.getCustomer()).isSameAs(customer);
    }

    @Test
    void updatePayment_multipleReferences_throws() {
        Payment existing = new Payment();
        when(paymentRepository.findById(5L)).thenReturn(Optional.of(existing));
        input.setSaleId(10L);
        input.setPurchaseId(20L);
        assertThatThrownBy(() -> service.updatePayment(5L, input)).isInstanceOf(RestApiException.class);
        verify(paymentRepository, never()).saveAndFlush(any(Payment.class));
    }

    @Test
    void deletePayment_sale_flushesBeforeStatusUpdate() {
        Payment p = payment(PaymentType.EINNAHME, "50");
        p.setSale(sale(10L, "100"));
        when(paymentRepository.findById(5L)).thenReturn(Optional.of(p));
        service.deletePayment(5L);
        org.mockito.InOrder order = inOrder(paymentRepository, saleService);
        order.verify(paymentRepository).delete(p);
        order.verify(paymentRepository).flush();
        order.verify(saleService).updatePaymentStatus(10L);
    }

    @Test
    void deletePayment_purchase_updatesStatus() {
        Payment p = payment(PaymentType.AUSGABE, "50");
        p.setPurchase(purchase(20L, "100"));
        when(paymentRepository.findById(5L)).thenReturn(Optional.of(p));
        service.deletePayment(5L);
        verify(paymentRepository).delete(p);
        verify(paymentRepository).flush();
        verify(purchaseService).updatePaymentStatus(20L);
    }

    @Test
    void deletePayment_notFound() {
        when(paymentRepository.findById(5L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.deletePayment(5L)).isInstanceOf(EntityNotFoundException.class);
        verify(paymentRepository, never()).delete(any(Payment.class));
    }

    @Test
    void getAllSaleIds() {
        when(paymentRepository.findDistinctSaleIds()).thenReturn(List.of(1L, 2L));
        assertThat(service.getAllSaleIds()).containsExactly(1L, 2L);
    }

    @Test
    void getAllPurchaseIds() {
        when(paymentRepository.findDistinctPurchaseIds()).thenReturn(List.of(3L));
        assertThat(service.getAllPurchaseIds()).containsExactly(3L);
    }
}
