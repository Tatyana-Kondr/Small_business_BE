package de.ait.smallBusiness_be.purchases.model;

import de.ait.smallBusiness_be.products.model.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * 15.01.2025
 * SmB
 *
 * @author Kondratyeva (AIT TR)
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "purchase_items")
public class PurchaseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
    @NotNull(message = "{validation.notNull}")
    Product product;

    @ManyToOne
    @JoinColumn(name = "purchase_id", referencedColumnName = "id", nullable = false)
    @NotNull(message = "{validation.notNull}")
    Purchase purchase;

    @Column(nullable = false)
    String productName;

    @Column(nullable = false)
    Integer quantity;

    @Column(precision = 8, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
    BigDecimal totalPrice;// Общая стоимость.

    @Column(precision = 2)
    @DecimalMin(value = "0", message = "{validation.tax.min}")
    BigDecimal taxPercentage;

    @Column(precision = 8, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
    BigDecimal taxAmount; // Сумма налога

    @Column(precision = 8, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
    BigDecimal totalAmount;
}
