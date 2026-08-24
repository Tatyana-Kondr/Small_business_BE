package de.ait.smallBusiness_be.sales.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "New Sale Item", description = "Information for creating a sale item")
public class NewSaleItemDto {

        @NotNull(message = "{validation.notNull}")
        @Schema(description = "Position of the item in the sale", example = "1")
        Integer position;

        @NotNull(message = "{validation.notNull}")
        @Positive(message = "{validation.positive}")
        @Schema(description = "Product ID", example = "16")
        Long productId;

        @NotBlank(message = "{validation.notBlank}")
        @Size(min = 1, max = 255, message = "{validation.name.size}")
        @Schema(description = "Product article", example = "WB3425")
        String productArticle;

        @NotBlank(message = "{validation.notBlank}")
        @Size(min = 3, max = 150, message = "{validation.name.size}")
        @Schema(description = "Product name", example = "Weinbox")
        String productName;

        @NotNull(message = "{validation.notNull}")
        @DecimalMin(value = "0.01", message = "{validation.price.min}")
        @Digits(integer = 10, fraction = 3, message = "{validation.quantity.digits}")
        @Schema(description = "Quantity of the product", example = "2")
        BigDecimal quantity;

        @NotNull(message = "{validation.notNull}")
        @DecimalMin(value = "0.0", inclusive = true, message = "{validation.price.min}")
        @Digits(integer = 10, fraction = 2, message = "{validation.price.digits}")
        @Schema(description = "Unit price of the product", example = "17.99")
        BigDecimal unitPrice;

        @NotNull(message = "{validation.notNull}")
        @DecimalMin(value = "0.0", message = "{validation.percentage.min}")
        @DecimalMax(value = "100.0", message = "{validation.percentage.max}")
        @Schema(description = "Discount percentage", example = "10")
        BigDecimal discount;

        @DecimalMin(value = "0", message = "{validation.tax.min}")
        @Schema(description = "Discount amount", example = "00.00")
        BigDecimal discountAmount;

        @DecimalMin(value = "0.0", message = "{validation.price.min}")
        @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
        @Schema(description = "Total price before tax", example = "00.00")
        BigDecimal totalPrice;

        @NotNull(message = "{validation.notNull}")
        @DecimalMin(value = "0.0", message = "{validation.percentage.min}")
        @DecimalMax(value = "100.0", message = "{validation.percentage.max}")
        @Schema(description = "Tax percentage", example = "19")
        BigDecimal tax;

        @DecimalMin(value = "0.0", message = "{validation.price.min}")
        @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
        @Schema(description = "Tax amount", example = "00.00")
        BigDecimal taxAmount;

        @DecimalMin(value = "0.0", message = "{validation.price.min}")
        @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
        @Schema(description = "Total amount after tax", example = "00.00")
        BigDecimal totalAmount;
}
