package de.ait.smallBusiness_be.sales.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Dimensions")
public class NewShippingDimensionsDto {

    @Digits(integer = 10, fraction = 2, message = "{validation.max.size}")
    @Schema(description = "Width of the package in sm", example = "10.00")
    BigDecimal width;

    @Digits(integer = 10, fraction = 2, message = "{validation.max.size}")
    @Schema(description = "Height of the package in sm", example = "10.00")
    BigDecimal height;

    @Digits(integer = 10, fraction = 2, message = "{validation.max.size}")
    @Schema(description = "Length of the package in sm", example = "10.00")
    BigDecimal length;

    @Digits(integer = 10, fraction = 2, message = "{validation.max.size}")
    @Schema(description = "Weight of the package in kg", example = "10.00")
    private BigDecimal weight;
}
