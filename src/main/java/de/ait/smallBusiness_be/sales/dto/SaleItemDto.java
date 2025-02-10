package de.ait.smallBusiness_be.sales.dto;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

public record SaleItemDto(
        Long id,
        Integer position,
        Long saleId,
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal discount,
        BigDecimal discountAmount,
        BigDecimal totalPrice,
        BigDecimal tax,
        BigDecimal taxAmount,
        BigDecimal totalAmount) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
