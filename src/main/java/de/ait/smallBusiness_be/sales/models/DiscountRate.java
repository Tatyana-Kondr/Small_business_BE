package de.ait.smallBusiness_be.sales.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(
        name = "discount_rates",
        uniqueConstraints = {@UniqueConstraint(name = "uk_discount_rates_value", columnNames = "value")})
public class DiscountRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "{validation.notNull}")
    @DecimalMin(value = "0.0", message = "{validation.percentage.min}")
    @DecimalMax(value = "100.0", message = "{validation.percentage.max}")
    @Digits(integer = 3, fraction = 2, message = "{validation.digits}")
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal value;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
