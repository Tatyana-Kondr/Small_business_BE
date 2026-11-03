package de.ait.smallBusiness_be.sales.services.impl;

import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.customers.services.CustomerService;
import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.payments.dao.PaymentRepository;
import de.ait.smallBusiness_be.payments.model.Payment;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.products.service.ProductService;
import de.ait.smallBusiness_be.purchases.model.PaymentStatus;
import de.ait.smallBusiness_be.purchases.model.TypeOfOperation;
import de.ait.smallBusiness_be.sales.dao.SaleRepository;
import de.ait.smallBusiness_be.sales.dao.ShippingRepository;
import de.ait.smallBusiness_be.sales.dto.NewSaleDto;
import de.ait.smallBusiness_be.sales.dto.NewSaleItemDto;
import de.ait.smallBusiness_be.sales.dto.NewShippingDimensionsDto;
import de.ait.smallBusiness_be.sales.dto.SaleDto;
import de.ait.smallBusiness_be.sales.models.*;
import de.ait.smallBusiness_be.sales.services.DocumentService;
import de.ait.smallBusiness_be.sales.services.SaleService;
import de.ait.smallBusiness_be.sales.services.TermOfPaymentService;
import de.ait.smallBusiness_be.warehouse.services.WarehouseService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


/**
 * 2/5/2025
 * Small_business_BE
 *
 * @author Chechkina (AIT TR)
 */


@RequiredArgsConstructor
@Service
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final PaymentRepository paymentRepository;
    private final ShippingRepository shippingRepository;
    private final TermOfPaymentService termOfPaymentService;
    private final CustomerService customerService;
    private final ProductService productService;
    private final DocumentService invoiceService;
    private final WarehouseService warehouseService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public SaleDto createSale(NewSaleDto newSale) {

        Customer customer = customerService.getCustomerOrThrow(newSale.getCustomerId());
        TermOfPayment termOfPayment = termOfPaymentService.getTermOfPaymentOrThrow(newSale.getTermsOfPaymentId());
        Shipping shipping = resolveShipping(newSale.getShippingId());

        // Генерация invoiceNumber и deliveryBill (если не передано)
        String invoiceNumber = trimToNull(newSale.getInvoiceNumber());
        String deliveryBill = trimToNull(newSale.getDeliveryBill());

        if (invoiceNumber == null || invoiceNumber.isBlank()) {
            invoiceNumber = generateUniqueInvoiceNumber();
            deliveryBill = invoiceNumber.replaceFirst("^RE", "LF");
        }

        // Маппинг в сущность + установка обязательных полей
        Sale sale = new Sale();

        sale.setCustomer(customer);
        sale.setShipping(shipping);
        sale.setSalesDate(newSale.getSalesDate());
        sale.setDeliveryDate(newSale.getDeliveryDate());
        sale.setTermsOfPayment(termOfPayment);
        sale.setInvoiceNumber(invoiceNumber);
        sale.setDeliveryBill(deliveryBill);
        sale.setAccountObject(trimToNull(newSale.getAccountObject()));
        sale.setOrderNumber(trimToNull(newSale.getOrderNumber()));
        sale.setOrderType(trimToNull(newSale.getOrderType()));
        sale.setTypeOfOperation(newSale.getTypeOfOperation());
        sale.setPaymentStatus(PaymentStatus.OFFEN);

        // Маппинг габаритов
        sale.setShippingDimensions(
                resolveShippingDimensions(newSale.getShippingId(), newSale.getShippingDimensions()));

        SaleCalculationResult calculation = buildSaleItems(sale, newSale.getSalesItems());

        sale.setSaleItems(calculation.items());
        sale.setDiscountAmount(calculation.discountAmount());
        sale.setTotalPrice(calculation.totalPrice());
        sale.setTaxAmount(calculation.taxAmount());
        sale.setTotalAmount(calculation.totalAmount());

        Sale savedSale = saleRepository.saveAndFlush(sale);

        Path invoiceTemp = null;
        Path deliveryBillTemp = null;

        try {
            /*
             * Сначала генерируем оба PDF.
             * Если генерация второго упадёт, первый временный файл
             * будет удалён в finally, а постоянные файлы ещё не созданы.
             */
            invoiceTemp = invoiceService.generateInvoiceTempPdf(savedSale, "invoices");
            deliveryBillTemp = invoiceService.generateDeliveryBillTempPdf(savedSale, "delivery-bill");

            /*
             * Только после успешной генерации обоих документов
             * перемещаем их в постоянные пути.
             */
            invoiceService.replaceInvoicePdf(savedSale, "invoices", invoiceTemp);
            invoiceTemp = null;

            invoiceService.replaceDeliveryBillPdf(savedSale, "delivery-bill", deliveryBillTemp);
            deliveryBillTemp = null;

        } finally {
            invoiceService.deleteTempFile(invoiceTemp);
            invoiceService.deleteTempFile(deliveryBillTemp);
        }

        // Обновляем склад
        savedSale.getSaleItems().forEach(item -> {
            warehouseService.recordOperation(
                    item.getProduct(),
                    savedSale.getTypeOfOperation(),
                    item.getQuantity(),
                    savedSale.getId(),
                    savedSale.getCustomer(),
                    savedSale.getSalesDate()
            );
        });

        return toSaleDto(savedSale);
    }

    @Override
    @Transactional
    public Page<SaleDto> getAllSales(Pageable pageable) {
        return saleRepository.findAllWithSorting(pageable)
                .map(this::toSaleDto);
    }

    @Override
    @Transactional
    public SaleDto getSaleById(Long id) {
        Sale sale = getSaleOrThrow(id);
        return toSaleDto(sale);
    }

    @Override
    @Transactional
    public SaleDto updateSale(Long saleId, NewSaleDto updateSale) {
        Sale sale = getSaleOrThrow(saleId);

        if (sale.getPaymentStatus() == PaymentStatus.BEZAHLT) {
            throw new RestApiException(ErrorDescription.SALE_UPDATE_ALREADY_PAID, HttpStatus.CONFLICT);
        }

        validateCustomerChange(sale, updateSale.getCustomerId());

        Customer customer = customerService.getCustomerOrThrow(updateSale.getCustomerId());
        TermOfPayment termOfPayment = termOfPaymentService.getTermOfPaymentOrThrow(updateSale.getTermsOfPaymentId());
        Shipping shipping = resolveShipping(updateSale.getShippingId());

        sale.setCustomer(customer);
        sale.setShipping(shipping);
        sale.setTermsOfPayment(termOfPayment);
        sale.setAccountObject(updateSale.getAccountObject());
        sale.setOrderNumber(updateSale.getOrderNumber());
        sale.setOrderType(updateSale.getOrderType());
        sale.setSalesDate(updateSale.getSalesDate());
        sale.setDeliveryDate(updateSale.getDeliveryDate());
        sale.setShippingDimensions(resolveShippingDimensions(updateSale.getShippingId(), updateSale.getShippingDimensions()));
        sale.setTypeOfOperation(updateSale.getTypeOfOperation());

        SaleCalculationResult calculation = buildSaleItems(sale, updateSale.getSalesItems());

        // Удаляем все старые позиции
        sale.getSaleItems().clear();
        sale.getSaleItems().addAll(calculation.items());

        sale.setDiscountAmount(calculation.discountAmount());
        sale.setTotalPrice(calculation.totalPrice());
        sale.setTaxAmount(calculation.taxAmount());
        sale.setTotalAmount(calculation.totalAmount());

        Sale updatedSale = saleRepository.saveAndFlush(sale);

        recalculatePaymentStatus(updatedSale);

        Path invoiceTemp = null;
        Path deliveryBillTemp = null;

        try {
            /*
             * Сначала генерируются оба новых документа.
             * Пока оба не созданы, старые PDF не затрагиваются.
             */
            invoiceTemp = invoiceService.generateInvoiceTempPdf(updatedSale, "invoices");

            deliveryBillTemp = invoiceService.generateDeliveryBillTempPdf(updatedSale, "delivery-bill");

            /*
             * Только после успешной генерации обоих файлов
             * заменяем действующие документы.
             */
            invoiceService.replaceInvoicePdf(updatedSale, "invoices", invoiceTemp);
            invoiceTemp = null;

            invoiceService.replaceDeliveryBillPdf(updatedSale, "delivery-bill", deliveryBillTemp);

            deliveryBillTemp = null;

        } finally {
            invoiceService.deleteTempFile(invoiceTemp);
            invoiceService.deleteTempFile(deliveryBillTemp);
        }

            // Синхронизируем документ со складом
        warehouseService.syncDocument(
                updatedSale.getTypeOfOperation(),
                updatedSale.getId(),
                updatedSale.getCustomer(),
                updatedSale.getSalesDate(),
                updatedSale.getSaleItems()
        );
        return toSaleDto(updatedSale);
    }

    @Override
    @Transactional
    public void deleteSale(Long saleId) {

        Sale sale = getSaleOrThrow(saleId);

        // 1. Полностью оплаченные продажи удалять нельзя
        if (sale.getPaymentStatus() == PaymentStatus.BEZAHLT) {
            throw new RestApiException(ErrorDescription.SALE_ALREADY_PAID, HttpStatus.CONFLICT);
        }

        // 2. Продажи с зарегистрированными платежами удалять нельзя
        if (paymentRepository.existsBySaleId(saleId)) {
            throw new RestApiException(ErrorDescription.SALE_HAS_PAYMENTS, HttpStatus.CONFLICT);
        }

        // 3. Удаляем PDF-документы
        invoiceService.deleteInvoicePdf(sale, "invoices");
        invoiceService.deleteDeliveryBillPdf(sale, "delivery-bill");

        // 4. Откатываем складские операции
        warehouseService.rollbackDocument(sale.getId(), List.of(TypeOfOperation.VERKAUF, TypeOfOperation.KUNDENERSTATTUNG));

        // 5. Удаляем продажу
        saleRepository.delete(sale);
    }

    @Override
    @Transactional
    public boolean checkIfSaleExistsById(Long saleId) {
        return saleRepository.existsById(saleId);
    }

    @Override
    @Transactional
    public SaleDto updatePaymentStatus(Long saleId) {

        Sale sale = getSaleOrThrow(saleId);

        recalculatePaymentStatus(sale);

        return toSaleDto(sale);
    }

    @Override
    @Transactional
    public Page<SaleDto> searchSales(Pageable pageable, String query) {
        return saleRepository.searchSales(pageable, query)
                .map(this::toSaleDto);
    }

    @Override
    @Transactional
    public Page<SaleDto> getAllSalesByFilter(Pageable pageable, Long id, Long customerId, String customerName, String invoiceNumber, BigDecimal totalAmount, String paymentStatus, LocalDate startDate, LocalDate endDate, String searchQuery) {
        return saleRepository.filterSalesByFields(pageable, id, customerId, customerName, invoiceNumber, totalAmount, paymentStatus, startDate, endDate, searchQuery)
                .map(this::toSaleDto);
    }

    private void recalculatePaymentStatus(Sale sale) {

        List<Payment> payments = paymentRepository.findBySaleId(sale.getId());

        BigDecimal totalPaid = calculateTotalPaid(payments);
        BigDecimal totalAmount = sale.getTotalAmount();

        // Дата последней платёжной операции
        LocalDate lastPaymentDate = payments.stream()
                .map(Payment::getPaymentDate)
                .filter(java.util.Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(null);

        // Статус оплаты
        if (totalPaid.compareTo(BigDecimal.ZERO) <= 0) {
            sale.setPaymentStatus(PaymentStatus.OFFEN);

        } else if (totalPaid.compareTo(totalAmount) < 0) {
            sale.setPaymentStatus(PaymentStatus.ANZAHLUNG);

        } else {
            sale.setPaymentStatus(PaymentStatus.BEZAHLT);
        }

        sale.setPaymentDate(lastPaymentDate);
    }


    public Sale getSaleOrThrow(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new RestApiException(ErrorDescription.SALE_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private String generateUniqueInvoiceNumber() {
        int attempts = 0;
        while (attempts < 5) {  // ограничение по числу попыток
            String candidate = generateInvoiceNumber();

            // Проверяем, существует ли уже такой номер
            boolean exists = saleRepository.existsByInvoiceNumber(candidate);
            if (!exists) {
                return candidate;
            }
            attempts++;
        }
        throw new RuntimeException("Failed to generate a unique invoice number");
    }

    private String generateInvoiceNumber() {
        int year = LocalDate.now().getYear();

        // Получаем максимальный номер за год с блокировкой или в транзакции
        Integer lastNumber = saleRepository.findLastInvoiceSequenceForYear(year);
        if (lastNumber == null) lastNumber = 0;

        int nextNumber = lastNumber + 1;
        String formattedNumber = String.format("%03d", nextNumber);

        return "RE" + year + formattedNumber;
    }

    private BigDecimal calculateTotalPaid(Long saleId) {
        List<Payment> payments = paymentRepository.findBySaleId(saleId);
        return calculateTotalPaid(payments);
    }

    private BigDecimal calculateTotalPaid(List<Payment> payments) {
        return payments.stream()
                .map(payment -> switch (payment.getType()) {
                    case EINNAHME -> payment.getAmount();
                    case AUSGABE -> payment.getAmount().negate();
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String normalizeNewlines(String s) {
        if (s == null) return null;
        return s.replace("\r\n", "\n").replace("\r", "\n");
    }

    private Shipping resolveShipping(Long shippingId) {
        if (shippingId == null) {
            return null;
        }
        return shippingRepository.findById(shippingId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.SHIPPING_NOT_FOUND.getDescription()));
    }

    private ShippingDimensions resolveShippingDimensions(Long shippingId, NewShippingDimensionsDto dimensionsDto) {
        if (shippingId == null || dimensionsDto == null) {
            return null;
        }
        return modelMapper.map(dimensionsDto, ShippingDimensions.class);
    }


    private void validateCustomerChange(Sale sale, Long newCustomerId) {

        Long currentCustomerId = sale.getCustomer().getId();

        if (currentCustomerId.equals(newCustomerId)) {
            return;
        }

        if (paymentRepository.existsBySaleId(sale.getId())) {
            throw new RestApiException(ErrorDescription.SALE_CUSTOMER_CHANGE_HAS_PAYMENTS, HttpStatus.CONFLICT);
        }
    }


    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private SaleCalculationResult buildSaleItems(Sale sale, List<NewSaleItemDto> itemDtos) {
        if (itemDtos == null || itemDtos.isEmpty()) {
            throw new RestApiException(ErrorDescription.NO_PRODUCT_IN_SALE, HttpStatus.BAD_REQUEST);
        }

        List<SaleItem> items = new ArrayList<>();

        BigDecimal discountAmountSum = BigDecimal.ZERO;
        BigDecimal totalPriceSum = BigDecimal.ZERO;
        BigDecimal taxAmountSum = BigDecimal.ZERO;
        BigDecimal totalAmountSum = BigDecimal.ZERO;

        int position = 1;

        for (NewSaleItemDto itemDto : itemDtos) {
            Product product = productService.getProductOrThrow(itemDto.getProductId());

            BigDecimal quantity = itemDto.getQuantity();
            BigDecimal unitPrice = itemDto.getUnitPrice();
            BigDecimal discount = itemDto.getDiscount();
            BigDecimal tax = itemDto.getTax();

            BigDecimal totalBeforeDiscount = unitPrice.multiply(quantity);

            BigDecimal discountAmount = totalBeforeDiscount
                    .multiply(discount.movePointLeft(2))
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal totalPrice = totalBeforeDiscount
                    .subtract(discountAmount)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal taxAmount = totalPrice
                    .multiply(tax.movePointLeft(2))
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal totalAmount = totalPrice
                    .add(taxAmount)
                    .setScale(2, RoundingMode.HALF_UP);

            SaleItem item = new SaleItem();
            item.setSale(sale);
            item.setProduct(product);
            item.setPosition(position++);
            item.setProductName(normalizeNewlines(itemDto.getProductName()));
            item.setQuantity(quantity);
            item.setUnitPrice(unitPrice);
            item.setDiscount(discount);
            item.setDiscountAmount(discountAmount);
            item.setTotalPrice(totalPrice);
            item.setTax(tax);
            item.setTaxAmount(taxAmount);
            item.setTotalAmount(totalAmount);

            items.add(item);

            discountAmountSum = discountAmountSum.add(discountAmount);
            totalPriceSum = totalPriceSum.add(totalPrice);
            taxAmountSum = taxAmountSum.add(taxAmount);
            totalAmountSum = totalAmountSum.add(totalAmount);
        }

        return new SaleCalculationResult(
                items,
                discountAmountSum.setScale(2, RoundingMode.HALF_UP),
                totalPriceSum.setScale(2, RoundingMode.HALF_UP),
                taxAmountSum.setScale(2, RoundingMode.HALF_UP),
                totalAmountSum.setScale(2, RoundingMode.HALF_UP)
        );
    }

    private record SaleCalculationResult(
            List<SaleItem> items,
            BigDecimal discountAmount,
            BigDecimal totalPrice,
            BigDecimal taxAmount,
            BigDecimal totalAmount
    ) {
    }

    private SaleDto toSaleDto(Sale sale) {

        SaleDto dto = modelMapper.map(sale, SaleDto.class);

        BigDecimal totalPaid = calculateTotalPaid(sale.getId());
        BigDecimal totalAmount = sale.getTotalAmount();

        dto.setTotalPaid(totalPaid);
        dto.setRemaining(totalAmount.subtract(totalPaid).max(BigDecimal.ZERO));
        dto.setOverpayment(totalPaid.subtract(totalAmount).max(BigDecimal.ZERO));
        dto.setHasPayments(paymentRepository.existsBySaleId(sale.getId()));

        return dto;
    }

}
