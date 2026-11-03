package de.ait.smallBusiness_be.products.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
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

    @DecimalMin(value = "0.0", message = "{validation.dimension.min}")
    @Digits(integer = 5, fraction = 2, message = "{validation.digits}")
    @Schema(description = "Width of the product", example = "10.00")
    private BigDecimal width;

    @DecimalMin(value = "0.0", message = "{validation.dimension.min}")
    @Digits(integer = 5, fraction = 2, message = "{validation.digits}")
    @Schema(description = "Height of the product", example = "10.00")
    private BigDecimal height;

    @DecimalMin(value = "0.0", message = "{validation.dimension.min}")
    @Digits(integer = 5, fraction = 2, message = "{validation.digits}")
    @Schema(description = "Length of the product", example = "10.00")
    private BigDecimal length;

    @DecimalMin(value = "0.0", message = "{validation.dimension.min}")
    @Digits(integer = 5, fraction = 2, message = "{validation.digits}")
    @Schema(description = "Diameter of the product", example = "10.00")
    private BigDecimal diameter;
}
