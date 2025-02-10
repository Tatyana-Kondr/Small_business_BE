package de.ait.smallBusiness_be.products.dto;

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
public class NewDimensionsDto {

    @Digits(integer = 10, fraction = 2, message = "{validation.max.size}")
    @Schema(description = "Width of the product", example = "10.00")
    BigDecimal width;

    @Digits(integer = 10, fraction = 2, message = "{validation.max.size}")
    @Schema(description = "Height of the product", example = "10.00")
    BigDecimal height;

    @Digits(integer = 10, fraction = 2, message = "{validation.max.size}")
    @Schema(description = "Length of the product", example = "10.00")
    BigDecimal length;
}
