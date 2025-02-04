package de.ait.smallBusiness_be.purchases.dto;

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

    @NotBlank(message = "{validation.notBlank}")
    @Schema(description = "Product's id", example = "1548")
    Long productId;

    @NotBlank(message = "{validation.notBlank}")
    @Schema(description = "Purchase's id", example = "1")
    Long purchaseId;

    @NotNull(message = "{validation.notNull}")
    @Schema(description = "Amount without percentage", example = "123.00")
    Integer quantity;

    @NotNull(message = "{validation.notNull}")
    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
    @Schema(description = "Amount without percentage", example = "10.00")
    BigDecimal totalPrice;

    @NotNull(message = "{validation.notNull}")
    @DecimalMin(value = "0", message = "{validation.tax.min}")
    @Schema(description = "Tax percentage", example = "19")
    BigDecimal taxPercentage;

    @NotNull(message = "{validation.notNull}")
    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
    @Schema(description = "Amount tax", example = "1.90")
    BigDecimal taxAmount;

    @NotNull(message = "{validation.notNull}")
    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
    @Schema(description = "Total amount", example = "11.90")
    BigDecimal totalAmount;
}
