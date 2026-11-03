package de.ait.smallBusiness_be.purchases.model;

import de.ait.smallBusiness_be.products.model.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
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

    @Column(precision = 9, scale = 3, nullable = false)
    @DecimalMin(value = "0.001", message = "{validation.positive}")
    @Digits(integer = 6, fraction = 3, message = "{validation.quantity.digits}")
    BigDecimal quantity;

    @NotNull(message = "{validation.notNull}")
    @Column(nullable = false, precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.money.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.price.digits}")
    BigDecimal unitPrice;

    @Column(nullable = false, precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.percentage.min}")
    @DecimalMax(value = "100.0", message = "{validation.percentage.max}")
    @Builder.Default
    BigDecimal discount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.money.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.amount.digits}")
    @Builder.Default
    BigDecimal discountAmount = BigDecimal.ZERO; // Сумма скидки

    @Column(nullable = false, precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.money.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.amount.digits}")
    BigDecimal totalPrice;// Общая стоимость.

    @Column(nullable = false, precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.percentage.min}")
    @DecimalMax(value = "100.0", message = "{validation.percentage.max}")
    BigDecimal taxPercentage;

    @Column(nullable = false, precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.money.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.amount.digits}")
    BigDecimal taxAmount; // Сумма налога

    @Column(nullable = false, precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.money.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.amount.digits}")
    BigDecimal totalAmount;

    @Column(nullable = false)
    Integer position;
}
