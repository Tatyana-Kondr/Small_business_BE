package de.ait.smallBusiness_be.sales.dto;

import de.ait.smallBusiness_be.purchases.model.PaymentStatus;
import de.ait.smallBusiness_be.sales.models.TermOfPayment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "SaleDto", description = "Data of Sale")
public class SaleDto {
    Long id;
    Long customerId;
    String customerName;
    String invoiceNumber;
    String accountObject;
    String typeOfOperation;
    Long shippingId;
    NewShippingDimensionsDto shippingDimensions;
    TermOfPayment termsOfPayment;
    LocalDate salesDate;
    PaymentStatus paymentStatus;
    boolean hasPayments;
    LocalDate paymentDate;
    String orderNumber;
    String orderType;
    LocalDate deliveryDate;
    String deliveryBill;
    BigDecimal discountAmount;
    BigDecimal totalPrice;
    BigDecimal taxAmount;
    BigDecimal totalAmount;
    BigDecimal totalPaid;    // Уже оплачено
    BigDecimal remaining;    // Осталось оплатить
    BigDecimal overpayment;  // Переплата
    List<SaleItemDto> saleItems;
}
