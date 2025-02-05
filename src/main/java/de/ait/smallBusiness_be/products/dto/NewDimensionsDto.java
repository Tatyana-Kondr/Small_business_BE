package de.ait.smallBusiness_be.products.dto;

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
public class NewDimensionsDto {

    @Digits(integer = 10, fraction = 2, message = "{validation.max.size}")
    BigDecimal width;

    @Digits(integer = 10, fraction = 2, message = "{validation.max.size}")
    BigDecimal height;

    @Digits(integer = 10, fraction = 2, message = "{validation.max.size}")
    BigDecimal length;

    @Digits(integer = 10, fraction = 2, message = "{validation.max.size}")
    private BigDecimal weight;
}
