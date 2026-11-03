package de.ait.smallBusiness_be.sales.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewTaxRateDto {

    @NotNull(message = "{validation.notNull}")
    @DecimalMin(value = "0.0", message = "{validation.percentage.min}")
    @DecimalMax(value = "100.0", message = "{validation.percentage.max}")
    @Digits(integer = 3, fraction = 2, message = "{validation.digits}")
    private BigDecimal value;

    private boolean active;

    private boolean defaultRate;
}
