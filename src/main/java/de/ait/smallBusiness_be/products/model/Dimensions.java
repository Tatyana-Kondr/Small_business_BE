package de.ait.smallBusiness_be.products.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.*;

import java.math.BigDecimal;

/**
 * 11/22/2024
 * Small_business_BE
 *
 * @author Chechkina (AIT TR)
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class Dimensions {

    @Column(precision = 7, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.dimension.min}")
    @Digits(integer = 5, fraction = 2, message = "{validation.digits}")
    private BigDecimal width;

    @Column(precision = 7, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.dimension.min}")
    @Digits(integer = 5, fraction = 2, message = "{validation.digits}")
    private BigDecimal height;

    @Column(precision = 7, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.dimension.min}")
    @Digits(integer = 5, fraction = 2, message = "{validation.digits}")
    private BigDecimal length;

    @Column(precision = 7, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.dimension.min}")
    @Digits(integer = 5, fraction = 2, message = "{validation.digits}")
    private BigDecimal diameter;
}
