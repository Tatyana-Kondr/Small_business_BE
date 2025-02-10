package de.ait.smallBusiness_be.sales.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Schema(name = "New Sale Item", description = "Information for creating a sale item")
public record NewSaleItemDto(

        @NotNull(message = "{validation.notNull}")
        @Schema(description = "Position of the item in the sale", example = "1")
        Integer position,

        @NotNull(message = "{validation.notNull}")
        @Schema(description = "Product ID", example = "1001")
        Long productId,

        @NotBlank(message = "{validation.notBlank}")
        @Size(min = 2, max = 255, message = "{validation.name.size}")
        @Schema(description = "Product name", example = "Laptop Dell XPS 15")
        String productName,

        @NotNull(message = "{validation.notNull}")
        @Min(value = 1, message = "{validation.constraints.Min.message}")
        @Schema(description = "Quantity of the product", example = "2")
        Integer quantity,

        @DecimalMin(value = "0.0", message = "{validation.price.min}")
        @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
        @Schema(description = "Unit price of the product", example = "1200.50")
        BigDecimal unitPrice,

        @DecimalMin(value = "0", message = "{validation.tax.min}")
        @Schema(description = "Discount percentage", example = "10")
        BigDecimal discount,

        @DecimalMin(value = "0", message = "{validation.tax.min}")
        @Schema(description = "Discount amount", example = "00.00")
        BigDecimal discountAmount,

        @DecimalMin(value = "0.0", message = "{validation.price.min}")
        @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
        @Schema(description = "Total price before tax", example = "00.00")
        BigDecimal totalPrice,

        @DecimalMin(value = "0", message = "{validation.tax.min}")
        @Schema(description = "Tax percentage", example = "19")
        BigDecimal tax,

        @DecimalMin(value = "0.0", message = "{validation.price.min}")
        @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
        @Schema(description = "Tax amount", example = "00.00")
        BigDecimal taxAmount,

        @DecimalMin(value = "0.0", message = "{validation.price.min}")
        @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
        @Schema(description = "Total amount after tax", example = "00.00")
        BigDecimal totalAmount) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
