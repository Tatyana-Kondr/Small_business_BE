package de.ait.smallBusiness_be.purchases.dto;

import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.purchases.model.Purchase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(name = "New PurchaseItem", description = "Data for registration of new purchaseItem")
public class NewPurchaseItemDto {

    @NotNull(message = "{validation.notNull}")
   // @Schema(description = "Product's id", example = "1548")
    Product product;

    @Schema(description = "Name of product", example = "Lampe")
    String productName;

    @NotNull(message = "{validation.notNull}")
    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 6, fraction = 3, message = "{validation.price.digits}")
    @Schema(description = "Quantity", example = "1")
    BigDecimal quantity;

    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
    @Schema(description = "Unit price", example = "10.00")
    BigDecimal unitPrice;

    @NotNull(message = "{validation.notNull}")
    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
    @Schema(description = "Amount without percentage", example = "0")
    BigDecimal totalPrice;

    @NotNull(message = "{validation.notNull}")
    @DecimalMin(value = "0", message = "{validation.tax.min}")
    @Schema(description = "Tax percentage", example = "19")
    BigDecimal taxPercentage;

    @NotNull(message = "{validation.notNull}")
    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
    @Schema(description = "Amount tax", example = "0")
    BigDecimal taxAmount;

    @NotNull(message = "{validation.notNull}")
    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
    @Schema(description = "Total amount", example = "0")
    BigDecimal totalAmount;

    @NotNull(message = "{validation.notNull}")
    @Schema(description = "Position", example = "1")
    Integer position;
}
