package de.ait.smallBusiness_be.sales.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 11/22/2024
 * Small_business_BE
 *
 * @author Chechkina (AIT TR)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class ShippingDimensions {
    @Column(precision = 8, scale = 3)
    private BigDecimal width;

    @Column(precision = 8, scale = 3)
    private BigDecimal height;

    @Column(precision = 8, scale = 3)
    private BigDecimal length;

    @Column(precision = 8, scale = 3)
    @DecimalMin(value = "0.0", message = "{validation.weight.min}")
    @Digits(integer = 5, fraction = 3, message = "{validation.weight.digits}")
    private BigDecimal weight;
}
