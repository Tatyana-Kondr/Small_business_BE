package de.ait.smallBusiness_be.sales.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "SaleItemDto", description = "Data of Sale Item")
public class SaleItemDto {

    Long id;
    Integer position;
    Long saleId;
    Long productId;
    String productName;
    BigDecimal quantity;
    BigDecimal unitPrice;
    BigDecimal discount;
    BigDecimal discountAmount;
    BigDecimal totalPrice;
    BigDecimal tax;
    BigDecimal taxAmount;
    BigDecimal totalAmount;
}
