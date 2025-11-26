package de.ait.smallBusiness_be.sales.dto;

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
    String paymentStatus;
    LocalDate paymentDate;
    String orderNumber;
    String orderType;
    LocalDate deliveryDate;
    String deliveryBill;
    BigDecimal defaultDiscount;
    BigDecimal discountAmount;
    BigDecimal totalPrice;
    BigDecimal defaultTax;
    BigDecimal taxAmount;
    BigDecimal totalAmount;
    List<SaleItemDto> saleItems;
}
