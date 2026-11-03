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
        @Positive(message = "{validation.positive}")
        @Schema(description = "Product ID", example = "16")
        Long productId;

        @NotBlank(message = "{validation.notBlank}")
        @Size(min = 3, max = 150, message = "{validation.name.size}")
        @Schema(description = "Product name", example = "Weinbox")
        String productName;

        @NotNull(message = "{validation.notNull}")
        @DecimalMin(value = "0.001", message = "{validation.positive}")
        @Digits(integer = 6, fraction = 3, message = "{validation.quantity.digits}")
        BigDecimal quantity;

        @NotNull(message = "{validation.notNull}")
        @DecimalMin(value = "0.0", message = "{validation.money.min}")
        @Digits(integer = 10, fraction = 2, message = "{validation.price.digits}")
        BigDecimal unitPrice;

        @NotNull(message = "{validation.notNull}")
        @DecimalMin(value = "0.0", message = "{validation.percentage.min}")
        @DecimalMax(value = "100.0", message = "{validation.percentage.max}")
        BigDecimal discount;

        @NotNull(message = "{validation.notNull}")
        @DecimalMin(value = "0.0", message = "{validation.percentage.min}")
        @DecimalMax(value = "100.0", message = "{validation.percentage.max}")
        BigDecimal tax;

}