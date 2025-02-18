package de.ait.smallBusiness_be.sales.services.impl;

import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.customers.services.CustomerService;
import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.products.service.ProductService;
import de.ait.smallBusiness_be.sales.dao.SaleRepository;
import de.ait.smallBusiness_be.sales.dto.NewSaleDto;
import de.ait.smallBusiness_be.sales.dto.SaleDto;
import de.ait.smallBusiness_be.sales.models.Sale;
import de.ait.smallBusiness_be.sales.models.SaleItem;
import de.ait.smallBusiness_be.sales.models.ShippingDimensions;
import de.ait.smallBusiness_be.sales.services.SaleService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
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

    private final CustomerService customerService;
    private final ProductService productService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public SaleDto createSale(NewSaleDto newSale) {

        Customer customer = customerService.getCustomerOrThrow(newSale.getCustomerId());

        Sale sale = modelMapper.map(newSale, Sale.class);
        sale.setCustomer(customer);
        sale.setShippingDimensions(newSale.getShippingDimensions() != null ?
                modelMapper.map(newSale.getShippingDimensions(), ShippingDimensions.class) :
                null);

        AtomicReference<BigDecimal> discountSum = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> subtotal = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> taxSum = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> total = new AtomicReference<>(BigDecimal.ZERO);

        if (newSale.getSalesItems()!=null && !newSale.getSalesItems().isEmpty()) {

            List<SaleItem> saleItems = newSale.getSalesItems().stream()
                    .map(newSaleItemDto -> {
                        Product product = productService.getProductOrThrow(newSaleItemDto.getProductId());
                        SaleItem saleItem = modelMapper.map(newSaleItemDto, SaleItem.class);
                        saleItem.setProduct(product);
                        saleItem.setSale(sale);

                        BigDecimal totalPriceWithoutDiscount = saleItem.getUnitPrice().multiply(saleItem.getQuantity()); //стоимость без скидки (цена * количество)
                        BigDecimal discountAmount = totalPriceWithoutDiscount
                                .multiply(saleItem.getDiscount().movePointLeft(2)) // Это эквивалентно делению на 100.
                                .setScale(2, RoundingMode.HALF_UP); // сумма скидки
                        BigDecimal totalPrice = totalPriceWithoutDiscount.subtract(discountAmount); // сумма с учетом скидки (стоимость - скидка)
                        BigDecimal taxAmount = totalPrice
                                .multiply(saleItem.getTax().movePointLeft(2)) // Это эквивалентно делению на 100.
                                .setScale(2, RoundingMode.HALF_UP); // сумма налога
                        BigDecimal totalAmount = totalPrice.add(taxAmount); // итоговая сумма с учетом скидки и налога (сумма с учетом скидки + налог)

                        saleItem.setDiscountAmount(discountAmount);
                        saleItem.setTotalPrice(totalPrice);
                        saleItem.setTaxAmount(taxAmount);
                        saleItem.setTotalAmount(totalAmount);

                        discountSum.updateAndGet(value -> value.add(saleItem.getDiscountAmount()));
                        subtotal.updateAndGet(value -> value.add(saleItem.getTotalPrice()));
                        taxSum.updateAndGet(value -> value.add(saleItem.getTaxAmount()));
                        total.updateAndGet(value -> value.add(saleItem.getTotalAmount()));
                        return saleItem;
                    }).toList();
            sale.setSalesItems(saleItems);
        }

        sale.setDiscountAmount(discountSum.get());
        sale.setTotalPrice(subtotal.get());
        sale.setTaxAmount(taxSum.get());
        sale.setTotalAmount(total.get());

        Sale savedSale = saleRepository.save(sale);

        return modelMapper.map(savedSale, SaleDto.class);
    }

    @Override
    public Page<SaleDto> getAllSales(Pageable pageable) {
        return null;
    }

    @Override
    public SaleDto getSaleById(Long id) {

        Sale sale = getSaleOrThrow(id);
        return modelMapper.map(sale, SaleDto.class);
    }

    @Override
    public SaleDto updateSale(Long saleId, NewSaleDto updateSale) {
        return null;
    }

    @Override
    public void deleteSale(Long saleId) {

    }

    public Sale getSaleOrThrow(Long id) {

        return saleRepository
                .findById(id)
                .orElseThrow(() -> new RestApiException(ErrorDescription.SALE_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    @Override
    public boolean checkIfSaleExistsById(Long saleId) {

        return saleRepository.existsById(saleId);
    }
}
