package de.ait.smallBusiness_be.purchases.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 15.01.2025
 * SmB
 *
 * @author Kondratyeva (AIT TR)
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "PurchaseItem", description = "Data of purchaseItem")
public class PurchaseItemDto {

    Long id;

    Long productId;

    Long purchaseId;

    Integer quantity;

    BigDecimal totalPrice;

    BigDecimal taxPercentage;

    BigDecimal taxAmount;

    BigDecimal totalAmount;
}
