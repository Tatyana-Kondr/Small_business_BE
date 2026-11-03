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
import de.ait.smallBusiness_be.purchases.dao.TypeOfDocumentRepository;
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
import org.springframework.data.domain.Pageable;
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
    private final TypeOfDocumentRepository typeOfDocumentRepository;
    private final PurchaseService purchaseService;
    private final SaleService saleService;
    private final ModelMapper modelMapper;


    @Override
    @Transactional(readOnly = true)
    public PaymentPrefillDto getPrefillDataForSale(Long saleId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.SALE_NOT_FOUND.getDescription()));

        List<Payment> payments = paymentRepository.findBySaleId(saleId);

        BigDecimal alreadyPaid = payments.stream()
                .map(payment -> switch (payment.getType()) {
                    case EINNAHME -> payment.getAmount();
                    case AUSGABE -> payment.getAmount().negate();
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal overpayment = alreadyPaid
                .subtract(sale.getTotalAmount())
                .max(BigDecimal.ZERO);

        BigDecimal amountLeft = sale.getTotalAmount().subtract(alreadyPaid).max(BigDecimal.ZERO);

        TypeOfDocument document = typeOfDocumentRepository.findByName("RECHNUNG")
                .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.TYPE_OF_DOCUMENT_NOT_FOUND.getDescription()));

        return PaymentPrefillDto.builder()
                .customerId(sale.getCustomer().getId())
                .customerName(sale.getCustomer().getName())
                .amount(sale.getTotalAmount())
                .amountLeft(amountLeft)
                .alreadyPaid(alreadyPaid)
                .overpayment(overpayment)
                .saleId(sale.getId())
                .documentId(document.getId())
                .documentName(document.getName())
                .documentNumber(sale.getInvoiceNumber())
                .type(PaymentType.EINNAHME)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentPrefillDto getPrefillDataForPurchase(Long purchaseId) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.PURCHASE_NOT_FOUND.getDescription()));

        List<Payment> payments = paymentRepository.findByPurchaseId(purchaseId);

        BigDecimal alreadyPaid = payments.stream()
                .map(payment -> switch (payment.getType()) {
                    case AUSGABE -> payment.getAmount();
                    case EINNAHME -> payment.getAmount().negate();
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal overpayment = alreadyPaid
                .subtract(purchase.getTotal())
                .max(BigDecimal.ZERO);

        BigDecimal amountLeft = purchase.getTotal().subtract(alreadyPaid).max(BigDecimal.ZERO);

        TypeOfDocument document = purchase.getDocument();

        return PaymentPrefillDto.builder()
                .customerId(purchase.getVendor().getId())
                .customerName(purchase.getVendor().getName())
                .amount(purchase.getTotal())
                .amountLeft(amountLeft)
                .alreadyPaid(alreadyPaid)
                .overpayment(overpayment)
                .purchaseId(purchase.getId())
                .documentId(document.getId())
                .documentName(document.getName())
                .documentNumber(purchase.getDocumentNumber())
                .type(PaymentType.AUSGABE)
                .build();
    }


    @Override
    @Transactional
    public PaymentDto createPayment(NewPaymentDto newPaymentDto) {

        validatePaymentReferences(newPaymentDto);

        PaymentReferences references = getPaymentReferences(newPaymentDto);

        Payment payment = new Payment();

        updatePaymentFields(payment, newPaymentDto, references);

        Payment saved = paymentRepository.saveAndFlush(payment);

        updateRelatedPaymentStatus(saved.getSale(), saved.getPurchase());

        return modelMapper.map(saved, PaymentDto.class);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDto> getPayments(Pageable pageable) {
        return paymentRepository
                .findAllWithSorting(pageable)
                .map(payment -> modelMapper.map(payment, PaymentDto.class));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDto> searchPayments(Pageable pageable, String query) {
        return paymentRepository.searchPayments(pageable, query)
                .map(payment -> modelMapper.map(payment, PaymentDto.class));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDto> getAllPaymentsByFilter(Pageable pageable, Long id, Long customerId, String customerName, Long saleId, Long purchaseId, LocalDate startDate, LocalDate endDate, Long documentId, String documentNumber, BigDecimal amount, String searchQuery) {
        return paymentRepository.filterByPaymentsFields(pageable, id, customerId, customerName, saleId, purchaseId, startDate, endDate, documentId, documentNumber, amount, searchQuery)
                .map(payment -> modelMapper.map(payment, PaymentDto.class));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDto getPayment(Long id) {
        return modelMapper.map(getPaymentOrThrow(id), PaymentDto.class);
    }


    @Override
    @Transactional
    public PaymentDto updatePayment(Long id, NewPaymentDto newPaymentDto) {
        Payment existing = getPaymentOrThrow(id);

        Long oldSaleId = existing.getSale() != null ? existing.getSale().getId() : null;

        Long oldPurchaseId = existing.getPurchase() != null ? existing.getPurchase().getId() : null;

        validatePaymentReferences(newPaymentDto);

        PaymentReferences references = getPaymentReferences(newPaymentDto);

        updatePaymentFields(existing, newPaymentDto, references);

        Payment saved = paymentRepository.saveAndFlush(existing);

        if (oldSaleId != null) {
            saleService.updatePaymentStatus(oldSaleId);
        }

        if (oldPurchaseId != null) {
            purchaseService.updatePaymentStatus(oldPurchaseId);
        }

        boolean saleChanged = saved.getSale() != null && !saved.getSale().getId().equals(oldSaleId);

        boolean purchaseChanged = saved.getPurchase() != null && !saved.getPurchase().getId().equals(oldPurchaseId);

        if (saleChanged || purchaseChanged) {
            updateRelatedPaymentStatus(saleChanged ? saved.getSale() : null, purchaseChanged ? saved.getPurchase() : null);
        }

        return modelMapper.map(saved, PaymentDto.class);
    }



    @Override
    @Transactional
    public void deletePayment(Long id) {

        Payment payment = getPaymentOrThrow(id);

        Sale sale = payment.getSale();
        Purchase purchase = payment.getPurchase();

        paymentRepository.delete(payment);
        paymentRepository.flush();

        updateRelatedPaymentStatus(sale, purchase);
    }


    @Override
    public List<Long> getAllSaleIds() {
        return paymentRepository.findDistinctSaleIds();
    }

    @Override
    public List<Long> getAllPurchaseIds() {
        return paymentRepository.findDistinctPurchaseIds();
    }


    private Payment getPaymentOrThrow(Long id) {
        return paymentRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(ErrorDescription.PAYMENT_NOT_FOUND.getDescription()));
    }

    private void validatePaymentReferences(NewPaymentDto newPaymentDto) {
        if (newPaymentDto.getSaleId() != null && newPaymentDto.getPurchaseId() != null) {
            throw new RestApiException(ErrorDescription.PAYMENT_MULTIPLE_REFERENCES, HttpStatus.CONFLICT);
        }
    }


    private PaymentReferences getPaymentReferences(NewPaymentDto newPaymentDto) {

        // Zahlungsmethode
        PaymentMethod paymentMethod = paymentMethodRepository.findById(newPaymentDto.getPaymentMethodId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.PAYMENT_METHOD_NOT_FOUND.getDescription()));

        // Zahlungsart
        PaymentProcess paymentProcess = paymentProcessRepository.findById(newPaymentDto.getPaymentProcessId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.PAYMENT_PROCESS_NOT_FOUND.getDescription()));

        // Dokument
        TypeOfDocument document = typeOfDocumentRepository.findById(newPaymentDto.getDocumentId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.TYPE_OF_DOCUMENT_NOT_FOUND.getDescription()));

        // Verkauf
        Sale sale = null;

        if (newPaymentDto.getSaleId() != null) {
            sale = saleRepository.findById(newPaymentDto.getSaleId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.SALE_NOT_FOUND.getDescription()));
        }

        // Einkauf
        Purchase purchase = null;

        if (newPaymentDto.getPurchaseId() != null) {
            purchase = purchaseRepository.findById(newPaymentDto.getPurchaseId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.PURCHASE_NOT_FOUND.getDescription()));
        }

        // Geschäftspartner automatisch bestimmen
        Customer customer;

        if (sale != null) {
            customer = sale.getCustomer();

        } else if (purchase != null) {
            customer = purchase.getVendor();

        } else {
            customer = customerRepository.findById(newPaymentDto.getCustomerId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.CUSTOMER_NOT_FOUND.getDescription()));
        }

        return new PaymentReferences(customer, paymentMethod, paymentProcess, document, sale, purchase);
    }

    private void updateRelatedPaymentStatus(Sale sale, Purchase purchase) {
        if (sale != null) {
            saleService.updatePaymentStatus(sale.getId());
        }

        if (purchase != null) {
            purchaseService.updatePaymentStatus(purchase.getId());
        }
    }


    private void updatePaymentFields(Payment payment, NewPaymentDto dto, PaymentReferences references) {
        payment.setPaymentDate(dto.getPaymentDate());
        payment.setCustomer(references.customer());
        payment.setType(dto.getType());
        payment.setAmount(dto.getAmount());
        payment.setSale(references.sale());
        payment.setPurchase(references.purchase());
        payment.setDocument(references.document());
        payment.setDocumentNumber(dto.getDocumentNumber());
        payment.setPaymentMethod(references.paymentMethod());
        payment.setPaymentProcess(references.paymentProcess());
    }

    private record PaymentReferences(
            Customer customer,
            PaymentMethod paymentMethod,
            PaymentProcess paymentProcess,
            TypeOfDocument document,
            Sale sale,
            Purchase purchase
    ) {
    }
}

