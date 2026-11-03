package de.ait.smallBusiness_be.productions.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.purchases.model.TypeOfOperation;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * 12.02.2025
 * SmB_be
 *
 * @author Kondratyeva (AIT TR)
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "production_items")
public class ProductionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
    @NotNull(message = "{validation.notNull}")
    Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_id", referencedColumnName = "id", nullable = false)
    @NotNull(message = "{validation.notNull}")
    @JsonBackReference
    Production production;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    TypeOfOperation type;

    @NotNull(message = "{validation.notNull}")
    @Column(precision = 9, scale = 3, nullable = false)
    @DecimalMin(value = "0.001", message = "{validation.positive}")
    @Digits(integer = 6, fraction = 3, message = "{validation.quantity.digits}")
    BigDecimal quantity;

    @NotNull(message = "{validation.notNull}")
    @Column(precision = 12, scale = 2, nullable = false)
    @DecimalMin(value = "0.0", message = "{validation.money.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.price.digits}")
    BigDecimal unitPrice;

    @NotNull(message = "{validation.notNull}")
    @Column(precision = 12, scale = 2, nullable = false)
    @DecimalMin(value = "0.01", message = "{validation.positive}")
    @Digits(integer = 10, fraction = 2, message = "{validation.amount.digits}")
    BigDecimal totalPrice;// Общая стоимость.
}
