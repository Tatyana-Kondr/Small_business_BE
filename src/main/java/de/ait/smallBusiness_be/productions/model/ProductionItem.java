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

    @ManyToOne
    @JoinColumn(name = "production_id", referencedColumnName = "id", nullable = false)
    @NotNull(message = "{validation.notNull}")
    @JsonBackReference
    Production production;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    TypeOfOperation type;

    @Column(nullable = false)
    Integer quantity;

    @Column(precision = 8, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
    BigDecimal unitPrice;

    @Column(precision = 8, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
    BigDecimal totalPrice;// Общая стоимость.
}
