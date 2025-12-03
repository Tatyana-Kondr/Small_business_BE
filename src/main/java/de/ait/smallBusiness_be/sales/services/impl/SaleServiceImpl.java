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
import de.ait.smallBusiness_be.sales.dao.TermOfPaymentRepository;
import de.ait.smallBusiness_be.sales.dto.NewSaleDto;
import de.ait.smallBusiness_be.sales.dto.NewSaleItemDto;
import de.ait.smallBusiness_be.sales.dto.SaleDto;
import de.ait.smallBusiness_be.sales.models.*;
import de.ait.smallBusiness_be.sales.services.DocumentService;
import de.ait.smallBusiness_be.sales.services.SaleService;
import de.ait.smallBusiness_be.warehouse.services.WarehouseService;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

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
    private final TermOfPaymentRepository termOfPaymentRepository;
    private final CustomerService customerService;
    private final ProductService productService;
    private final DocumentService invoiceService;
    private final WarehouseService warehouseService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public SaleDto createSale(NewSaleDto newSale) {

        Customer customer = customerService.getCustomerOrThrow(newSale.getCustomerId());
        Shipping shipping = null;
        if (newSale.getShippingId() != null) {
            shipping = shippingRepository.findById(newSale.getShippingId())
                    .orElseThrow(() -> new EntityNotFoundException("Shipping not found"));
        }

        TermOfPayment termOfPayment = termOfPaymentRepository.findById(newSale.getTermsOfPaymentId())
                .orElseThrow(() -> new EntityNotFoundException("Term of payment not found."));

        // Генерация invoiceNumber и deliveryBill (если не передано)
        String invoiceNumber = newSale.getInvoiceNumber();
        String deliveryBill = newSale.getDeliveryBill();

        if (invoiceNumber == null || invoiceNumber.isBlank()) {
            invoiceNumber = generateUniqueInvoiceNumber();
            deliveryBill = invoiceNumber.replaceFirst("^RE", "LF");
        }

        // Маппинг в сущность + установка обязательных полей
        Sale sale = modelMapper.map(newSale, Sale.class);
        sale.setCustomer(customer);
        sale.setShipping(shipping);
        sale.setTermsOfPayment(termOfPayment);
        sale.setInvoiceNumber(invoiceNumber);
        sale.setDeliveryBill(deliveryBill);
        sale.setDefaultTax(newSale.getDefaultTax());
        sale.setDefaultDiscount(newSale.getDefaultDiscount());

        // Маппинг габаритов
        if (newSale.getShippingId() != null && newSale.getShippingDimensions() != null) {
            sale.setShippingDimensions(
                    modelMapper.map(newSale.getShippingDimensions(), ShippingDimensions.class)
            );
        } else {
            sale.setShippingDimensions(null);
        }

        // Расчёт сумм
        AtomicReference<BigDecimal> discountSum = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> subtotal = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> taxSum = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> total = new AtomicReference<>(BigDecimal.ZERO);

        if (newSale.getSalesItems() != null && !newSale.getSalesItems().isEmpty()) {
            List<SaleItem> saleItems = newSale.getSalesItems().stream()
                    .map(newSaleItemDto -> {
                        Product product = productService.getProductOrThrow(newSaleItemDto.getProductId());
                        SaleItem saleItem = modelMapper.map(newSaleItemDto, SaleItem.class);
                        saleItem.setProduct(product);
                        saleItem.setSale(sale);

                        BigDecimal unitPrice = saleItem.getUnitPrice();
                        BigDecimal quantity = saleItem.getQuantity();

                        BigDecimal taxPercent = saleItem.getTax();
                        // 👇 Если налог в позиции отсутствует — берём из defaultTax
                        if (taxPercent == null) {
                            taxPercent = sale.getDefaultTax();
                            saleItem.setTax(taxPercent);
                        }

                        BigDecimal discountPercent = saleItem.getDiscount();
                        // 👇 Если скидка в позиции отсутствует — берём из defaultDiscount
                        if (discountPercent == null) {
                            discountPercent = sale.getDefaultDiscount();
                            saleItem.setDiscount(discountPercent);
                        }

                        BigDecimal totalPriceWithoutDiscount = unitPrice.multiply(quantity);
                        BigDecimal discountAmount = totalPriceWithoutDiscount
                                .multiply(saleItem.getDiscount().movePointLeft(2))
                                .setScale(2, RoundingMode.HALF_UP);
                        BigDecimal totalPrice = totalPriceWithoutDiscount.subtract(discountAmount);
                        BigDecimal taxAmount = totalPrice
                                .multiply(saleItem.getTax().movePointLeft(2))
                                .setScale(2, RoundingMode.HALF_UP);
                        BigDecimal totalAmount = totalPrice.add(taxAmount);

                        saleItem.setDiscountAmount(discountAmount);
                        saleItem.setTotalPrice(totalPrice);
                        saleItem.setTaxAmount(taxAmount);
                        saleItem.setTotalAmount(totalAmount);

                        discountSum.updateAndGet(val -> val.add(discountAmount));
                        subtotal.updateAndGet(val -> val.add(totalPrice));
                        taxSum.updateAndGet(val -> val.add(taxAmount));
                        total.updateAndGet(val -> val.add(totalAmount));

                        return saleItem;
                    }).toList();

            sale.setSaleItems(saleItems);
        }

        sale.setDiscountAmount(discountSum.get());
        sale.setTotalPrice(subtotal.get());
        sale.setTaxAmount(taxSum.get());
        sale.setTotalAmount(total.get());

        Sale savedSale = saleRepository.save(sale);
        invoiceService.generateInvoicePdf(savedSale, "invoices");
        invoiceService.generateDeliveryBillPdf(savedSale, "delivery-bill");

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

        return modelMapper.map(savedSale, SaleDto.class);
    }

    @Override
    @Transactional
    public Page<SaleDto> getAllSales(Pageable pageable) {

        // Проверяем, корректно ли передана сортировка
        List<String> allowedSortFields = List.of("salesDate", "invoiceNumber", "totalAmount"); // допустимые поля
        Sort sort = pageable.getSort();

        for (Sort.Order order : sort) {
            if (!allowedSortFields.contains(order.getProperty())) {
                // Если поле неверное, заменяем сортировку по умолчанию
                pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "salesDate"));
                break;
            }
        }

        Page<Sale> sales = saleRepository.findAll(pageable);

        if (sales.isEmpty()) {
            throw new RestApiException(ErrorDescription.LIST_IS_EMPTY, HttpStatus.NOT_FOUND);
        }

        return sales.map(sale -> modelMapper.map(sale, SaleDto.class));
    }

    @Override
    @Transactional
    public SaleDto getSaleById(Long id) {
        Sale sale = getSaleOrThrow(id);
        return modelMapper.map(sale, SaleDto.class);
    }

    @Override
    @Transactional
    public SaleDto updateSale(Long saleId, NewSaleDto updateSale) {
        Sale sale = getSaleOrThrow(saleId);

        // --- удаляем старые PDF ---
        invoiceService.deleteInvoicePdf(sale, "invoices");
        invoiceService.deleteDeliveryBillPdf(sale, "delivery-bill");

        Customer customer = customerService.getCustomerOrThrow(updateSale.getCustomerId());
        Shipping shipping = null;
        if (updateSale.getShippingId() != null) {
            shipping = shippingRepository.findById(updateSale.getShippingId())
                    .orElseThrow(() -> new EntityNotFoundException("Shipping not found"));
        }
        TermOfPayment termOfPayment = termOfPaymentRepository.findById(updateSale.getTermsOfPaymentId())
                .orElseThrow(() -> new EntityNotFoundException("Term of payment not found."));

        sale.setCustomer(customer);
        sale.setShipping(shipping);
        sale.setTermsOfPayment(termOfPayment);
        sale.setInvoiceNumber(updateSale.getInvoiceNumber());
        sale.setDeliveryBill(updateSale.getDeliveryBill());
        sale.setAccountObject(updateSale.getAccountObject());
        sale.setSalesDate(updateSale.getSalesDate());
        sale.setDefaultTax(updateSale.getDefaultTax());
        sale.setDefaultDiscount(updateSale.getDefaultDiscount());

        // --- ГАБАРИТЫ: только если shipping выбран ---
        if (updateSale.getShippingId() != null && updateSale.getShippingDimensions() != null) {
            sale.setShippingDimensions(
                    modelMapper.map(updateSale.getShippingDimensions(), ShippingDimensions.class)
            );
        } else {
            sale.setShippingDimensions(null);
        }
        try {
            sale.setTypeOfOperation(TypeOfOperation.valueOf(updateSale.getTypeOfOperation().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid type of operation: " + updateSale.getTypeOfOperation());
        }

        try {
            sale.setPaymentStatus(PaymentStatus.valueOf(updateSale.getPaymentStatus().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid payment status: " + updateSale.getPaymentStatus());
        }

        // Удаляем все старые позиции
        sale.getSaleItems().clear();
        // Пересчет сумм
        AtomicReference<BigDecimal> discountAmount = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> totalPrice = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> taxAmount = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> totalAmount = new AtomicReference<>(BigDecimal.ZERO);

        List<SaleItem> newItems = new ArrayList<>();
        int position = 1;

        for (NewSaleItemDto itemDto : updateSale.getSalesItems()) {
            Product product = productService.getProductOrThrow(itemDto.getProductId());

            SaleItem item = new SaleItem();
            item.setSale(sale);
            item.setProduct(product);
            item.setProductName(itemDto.getProductName());
            item.setQuantity(itemDto.getQuantity());
            item.setUnitPrice(itemDto.getUnitPrice());
            item.setDiscount(itemDto.getDiscount());
            item.setTax(itemDto.getTax());
            item.setPosition(position++);

            // Расчёты
            BigDecimal unitPrice = item.getUnitPrice();
            BigDecimal quantity = item.getQuantity();

            BigDecimal totalPriceWithoutDiscount = unitPrice.multiply(quantity); //стоимость без скидки (цена * количество)
            BigDecimal discountSum = totalPriceWithoutDiscount
                    .multiply(item.getDiscount().movePointLeft(2)) // Это эквивалентно делению на 100.
                    .setScale(2, RoundingMode.HALF_UP); // сумма скидки
            BigDecimal totalPriceWithDiscount = totalPriceWithoutDiscount.subtract(discountSum); // сумма с учетом скидки (стоимость - скидка)
            BigDecimal taxSum = totalPriceWithDiscount
                    .multiply(item.getTax().movePointLeft(2)) // Это эквивалентно делению на 100
                    .setScale(2, RoundingMode.HALF_UP); // сумма налога
            BigDecimal totalSum = totalPriceWithDiscount.add(taxSum); // итоговая сумма с учетом скидки и налога (сумма с учетом скидки + налог)

            item.setDiscountAmount(discountSum);
            item.setTotalPrice(totalPriceWithDiscount);
            item.setTaxAmount(taxSum);
            item.setTotalAmount(totalSum);

            discountAmount.updateAndGet(v -> v.add(item.getDiscountAmount()));
            totalPrice.updateAndGet(v -> v.add(item.getTotalPrice()));
            taxAmount.updateAndGet(v -> v.add(item.getTaxAmount()));
            totalAmount.updateAndGet(v -> v.add(item.getTotalAmount()));

            newItems.add(item);
        }

        // Добавляем новые позиции
        sale.getSaleItems().addAll(newItems);

        // Обновляем итоги
        sale.setSalesDate(updateSale.getSalesDate());
        sale.setTotalPrice(totalPrice.get());
        sale.setTaxAmount(taxAmount.get());
        sale.setTotalAmount(totalAmount.get());

        // Сохраняем
        Sale updatedSale = saleRepository.save(sale);
        invoiceService.generateInvoicePdf(updatedSale, "invoices");
        invoiceService.generateDeliveryBillPdf(updatedSale, "delivery-bill");

        // Синхронизируем документ со складом
        warehouseService.syncDocument(
                updatedSale.getTypeOfOperation(),
                updatedSale.getId(),
                updatedSale.getCustomer(),
                updatedSale.getSalesDate(),
                updatedSale.getSaleItems()
        );
        return modelMapper.map(updatedSale, SaleDto.class);
    }

    @Override
    public void deleteSale(Long saleId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new EntityNotFoundException("Sale not found"));
        invoiceService.deleteInvoicePdf(sale, "invoices");
        invoiceService.deleteDeliveryBillPdf(sale, "delivery-bill");
        warehouseService.rollbackDocument(sale.getId());
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
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new EntityNotFoundException("Sale not found"));

        // Получаем список оплат по saleId
        List<Payment> payments = paymentRepository.findBySaleId(saleId);

        BigDecimal totalPaid = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PaymentStatus newStatus;
        if (totalPaid.compareTo(BigDecimal.ZERO) == 0) {
            newStatus = PaymentStatus.AUSSTEHEND;
        } else if (totalPaid.compareTo(sale.getTotalAmount()) >= 0) {
            newStatus = PaymentStatus.BEZAHLT;
        } else {
            newStatus = PaymentStatus.ANZAHLUNG;
        }

        // Обновляем статус и дату оплаты, если необходимо
        if (sale.getPaymentStatus() != newStatus) {
            sale.setPaymentStatus(newStatus);

            if (newStatus == PaymentStatus.BEZAHLT) {
                // Ищем дату последней оплаты
                Optional<LocalDate> lastPaymentDate = payments.stream()
                        .map(Payment::getPaymentDate)
                        .max(LocalDate::compareTo);

                lastPaymentDate.ifPresent(sale::setPaymentDate);
            }

            saleRepository.save(sale);
        }

        return modelMapper.map(sale, SaleDto.class);
    }

    @Override
    @Transactional
    public Page<SaleDto> searchSales(Pageable pageable, String query) {
        return saleRepository.searchSales(pageable,query)
                .map(sale -> modelMapper.map(sale, SaleDto.class));
    }

    @Override
    @Transactional
    public Page<SaleDto> getAllSalesByFilter(Pageable pageable, Long id, Long customerId, String customerName, String invoiceNumber, BigDecimal totalAmount, String paymentStatus, LocalDate startDate, LocalDate endDate, String searchQuery) {
        return saleRepository.filterSalesByFields(pageable, id, customerId, customerName, invoiceNumber, totalAmount, paymentStatus, startDate, endDate, searchQuery)
                .map(sale -> modelMapper.map(sale, SaleDto.class));
    }

    public Sale getSaleOrThrow(Long id) {
        return saleRepository
                .findById(id)
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

}
