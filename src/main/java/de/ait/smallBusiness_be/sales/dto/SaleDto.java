package de.ait.smallBusiness_be.sales.dto;

import de.ait.smallBusiness_be.products.dto.NewDimensionsDto;
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
    String invoiceNumber;
    String accountObject;
    String typeOfOperation;
    String shipping;
    NewDimensionsDto shippingDimensions;
    String termsOfPayment;
    LocalDate salesDate;
    LocalDate paymentDate;
    String paymentStatus;
    BigDecimal discountAmount;
    BigDecimal totalPrice;
    BigDecimal taxAmount;
    BigDecimal totalAmount;
    List<SaleItemDto> salesItems;
}
