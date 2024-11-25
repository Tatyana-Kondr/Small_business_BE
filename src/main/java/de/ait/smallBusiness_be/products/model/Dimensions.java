package de.ait.smallBusiness_be.products.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
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
public class Dimensions {
    @Column(precision = 8, scale = 3)
    private BigDecimal width;

    @Column(precision = 8, scale = 3)
    private BigDecimal height;

    @Column(precision = 8, scale = 3)
    private BigDecimal length;
}
