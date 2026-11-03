package de.ait.smallBusiness_be.purchases.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
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
    @Positive(message = "{validation.positive}")
    @Schema(description = "Product's id", example = "1548")
    Long productId;

    @NotBlank(message = "{validation.notBlank}")
    @Size(min = 2, max = 255, message = "{validation.name.size}")
    @Schema(description = "Name of product", example = "Lampe")
    String productName;

    @NotNull(message = "{validation.notNull}")
    @DecimalMin(value = "0.001", message = "{validation.positive}")
    @Digits(integer = 6, fraction = 3, message = "{validation.quantity.digits}")
    BigDecimal quantity;

    @NotNull(message = "{validation.notNull}")
    @DecimalMin(value = "0.0", message = "{validation.money.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.price.digits}")
    @Schema(description = "Unit price", example = "10.00")
    BigDecimal unitPrice;

    @NotNull(message = "{validation.notNull}")
    @DecimalMin(value = "0.0", message = "{validation.percentage.min}")
    @DecimalMax(value = "100.0", message = "{validation.percentage.max}")
    @Schema(description = "Discount percentage", example = "10.00")
    BigDecimal discount;

    @NotNull(message = "{validation.notNull}")
    @DecimalMin(value = "0.0", message = "{validation.percentage.min}")
    @DecimalMax(value = "100.0", message = "{validation.percentage.max}")
    @Schema(description = "Tax percentage", example = "19")
    BigDecimal taxPercentage;

}
