package de.ait.smallBusiness_be.products.dto;

import jakarta.validation.constraints.Size;
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
    @Size(max = 20, message = "{validation.max.size}")
    BigDecimal width;

    @Size(max = 20, message = "{validation.max.size}")
    BigDecimal height;

    @Size(max = 20, message = "{validation.max.size}")
    BigDecimal length;

}
