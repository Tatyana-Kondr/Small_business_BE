package de.ait.smallBusiness_be.purchases.dto;

import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.purchases.model.Purchase;
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

    Product product;

    Long purchaseId;

    Integer quantity;

    BigDecimal unitPrice;

    BigDecimal totalPrice;

    BigDecimal taxPercentage;

    BigDecimal taxAmount;

    BigDecimal totalAmount;

    Integer position;
}
