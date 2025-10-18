package de.ait.smallBusiness_be.payments.services.impl;

import de.ait.smallBusiness_be.customers.dao.CustomerRepository;
import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
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
import de.ait.smallBusiness_be.payments.services.PaymentService;
import de.ait.smallBusiness_be.purchases.dao.PurchaseRepository;
import de.ait.smallBusiness_be.purchases.model.Purchase;
import de.ait.smallBusiness_be.purchases.model.TypeOfDocument;
import de.ait.smallBusiness_be.purchases.services.PurchaseService;
import de.ait.smallBusiness_be.sales.dao.SaleRepository;
import de.ait.smallBusiness_be.sales.models.Sale;
import de.ait.smallBusiness_be.sales.services.SaleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;
    private final PurchaseRepository purchaseRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentProcessRepository paymentProcessRepository;
    private final PurchaseService purchaseService;
    private final SaleService saleService;
    private final ModelMapper modelMapper;


    @Override
    public PaymentPrefillDto getPrefillDataForSale(Long saleId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new EntityNotFoundException("Sale not found with id: " + saleId));

        List<Payment> payments = paymentRepository.findBySaleId(saleId);

        BigDecimal alreadyPaid = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal amountLeft = sale.getTotalAmount().subtract(alreadyPaid).max(BigDecimal.ZERO);

        return PaymentPrefillDto.builder()
                .customerId(sale.getCustomer().getId())
                .customerName(sale.getCustomer().getName())
                .amount(sale.getTotalAmount())
                .amountLeft(amountLeft)
                .saleId(sale.getId())
                .document(TypeOfDocument.RECHNUNG.name())
                .documentNumber(sale.getInvoiceNumber())
                .type(PaymentType.EINNAHME)
                .build();
    }

    @Override
    public PaymentPrefillDto getPrefillDataForPurchase(Long purchaseId) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new EntityNotFoundException("Purchase not found with id: " + purchaseId));

        List<Payment> payments = paymentRepository.findByPurchaseId(purchaseId);

        BigDecimal alreadyPaid = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal amountLeft = purchase.getTotal().subtract(alreadyPaid).max(BigDecimal.ZERO);

        return PaymentPrefillDto.builder()
                .customerId(purchase.getVendor().getId())
                .customerName(purchase.getVendor().getName())
                .amount(purchase.getTotal())
                .amountLeft(amountLeft)
                .purchaseId(purchase.getId())
                .document(purchase.getDocument().name())
                .documentNumber(purchase.getDocumentNumber())
                .type(PaymentType.AUSGABE)
                .build();
    }

    @Override
    public PaymentDto createPayment(NewPaymentDto newPaymentDto) {

        Customer customer = customerRepository.findById(newPaymentDto.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        PaymentMethod method = paymentMethodRepository.findById(newPaymentDto.getPaymentMethodId())
                .orElseThrow(() -> new EntityNotFoundException("Payment method not found"));

        PaymentProcess process = paymentProcessRepository.findById(newPaymentDto.getPaymentProcessId())
                .orElseThrow(() -> new EntityNotFoundException("Payment process not found"));

        Sale sale = null;
        if (newPaymentDto.getSaleId() != null) {
            sale = saleRepository.findById(newPaymentDto.getSaleId())
                    .orElseThrow(() -> new EntityNotFoundException("Sale not found"));
        }

        Purchase purchase = null;
        if (newPaymentDto.getPurchaseId() != null) {
            purchase = purchaseRepository.findById(newPaymentDto.getPurchaseId())
                    .orElseThrow(() -> new EntityNotFoundException("Purchase not found"));
        }

        Payment payment = new Payment();
        payment.setPaymentDate(newPaymentDto.getPaymentDate());
        payment.setCustomer(customer);
        payment.setType(newPaymentDto.getType());
        payment.setAmount(newPaymentDto.getAmount());
        payment.setSale(sale);
        payment.setPurchase(purchase);
        payment.setDocument(TypeOfDocument.valueOf(newPaymentDto.getDocument()));
        payment.setDocumentNumber(newPaymentDto.getDocumentNumber());
        payment.setPaymentMethod(method);
        payment.setPaymentProcess(process);

        Payment saved = paymentRepository.save(payment);
        return modelMapper.map(saved, PaymentDto.class);
    }


    @Override
    public Page<PaymentDto> getPayments(Pageable pageable) {
        // Проверяем, корректно ли передана сортировка
        List<String> allowedSortFields = List.of("paymentDate");
        Sort sort = pageable.getSort();
        for (Sort.Order order : sort) {
            if (!allowedSortFields.contains(order.getProperty())) {
                pageable = PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(
                                Sort.Order.desc("paymentDate")
                        )
                );
                break;
            }
        }
        Page<Payment> payments = paymentRepository.findAll(pageable);
        if (payments.isEmpty()) {
            throw new RestApiException(ErrorDescription.LIST_PAYMENTS_IS_EMPTY, HttpStatus.NOT_FOUND);
        }
        return payments.map(payment -> modelMapper.map(payment, PaymentDto.class));
    }

    @Override
    public Page<PaymentDto> searchPayments(Pageable pageable, String query) {
        return paymentRepository.searchPayments(pageable, query)
                .map(payment -> modelMapper.map(payment, PaymentDto.class));
    }

    @Override
    public Page<PaymentDto> getAllPaymentsByFilter(Pageable pageable, Long id, Long customerId, String customerName, Long saleId, Long purchaseId, LocalDate startDate, LocalDate endDate, String document, String documentNumber, BigDecimal amount, String searchQuery) {
        return paymentRepository.filterByPaymentsFields(pageable, id, customerId, customerName, saleId, purchaseId, startDate, endDate, document, documentNumber, amount, searchQuery)
                .map(payment -> modelMapper.map(payment, PaymentDto.class));
    }

    @Override
    public PaymentDto getPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));
        return modelMapper.map(payment, PaymentDto.class);
    }

    @Override
    public PaymentDto updatePayment(Long id, NewPaymentDto newPaymentDto) {
        Payment existing = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));

        Customer customer = customerRepository.findById(newPaymentDto.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        PaymentMethod method = paymentMethodRepository.findById(newPaymentDto.getPaymentMethodId())
                .orElseThrow(() -> new EntityNotFoundException("Payment method not found"));

        PaymentProcess process = paymentProcessRepository.findById(newPaymentDto.getPaymentProcessId())
                .orElseThrow(() -> new EntityNotFoundException("Payment process not found"));

        Sale sale = null;
        if (newPaymentDto.getSaleId() != null) {
            sale = saleRepository.findById(newPaymentDto.getSaleId())
                    .orElseThrow(() -> new EntityNotFoundException("Sale not found"));
        }

        Purchase purchase = null;
        if (newPaymentDto.getPurchaseId() != null) {
            purchase = purchaseRepository.findById(newPaymentDto.getPurchaseId())
                    .orElseThrow(() -> new EntityNotFoundException("Purchase not found"));
        }

        existing.setPaymentDate(newPaymentDto.getPaymentDate());
        existing.setCustomer(customer);
        existing.setType(newPaymentDto.getType());
        existing.setAmount(newPaymentDto.getAmount());
        existing.setSale(sale);
        existing.setPurchase(purchase);
        existing.setDocument(TypeOfDocument.valueOf(newPaymentDto.getDocument()));
        existing.setDocumentNumber(newPaymentDto.getDocumentNumber());
        existing.setPaymentMethod(method);
        existing.setPaymentProcess(process);

        return modelMapper.map(paymentRepository.save(existing), PaymentDto.class);
    }

    @Override
    @Transactional
    public void deletePayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));

        paymentRepository.deleteById(id);

        if (payment.getPurchase() != null) {
            purchaseService.updatePaymentStatus(payment.getPurchase().getId());
        } else if (payment.getSale() != null) {
            saleService.updatePaymentStatus(payment.getSale().getId());
        }
    }

    @Override
    public List<Long> getAllSaleIds() {
        return paymentRepository.findDistinctSaleIds();
    }

    @Override
    public List<Long> getAllPurchaseIds() {
        return paymentRepository.findDistinctPurchaseIds();
    }
}