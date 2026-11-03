package de.ait.smallBusiness_be.sales.models;

import de.ait.smallBusiness_be.products.model.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * 2/5/2025
 * Small_business_BE
 *
 * @author Chechkina (AIT TR)
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "SaleItems")
public class SaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer position;

    @ManyToOne
    @JoinColumn(name = "sale_id", referencedColumnName = "id", nullable = false)
    @NotNull(message = "{validation.notNull}")
    private Sale sale;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
    @NotNull(message = "{validation.notNull}")
    private Product product;

    @Column(nullable = false)
    private String productName;

    @NotNull(message = "{validation.notNull}")
    @Column(precision = 9, scale = 3, nullable = false)
    @DecimalMin(value = "0.001", message = "{validation.positive}")
    @Digits(integer = 6, fraction = 3, message = "{validation.quantity.digits}")
    private BigDecimal quantity;

    @NotNull(message = "{validation.notNull}")
    @Column(nullable = false, precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.money.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.price.digits}")
    private BigDecimal unitPrice;

    @NotNull(message = "{validation.notNull}")
    @Column(nullable = false, precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.percentage.min}")
    @DecimalMax(value = "100.0", message = "{validation.percentage.max}")
    @Digits(integer = 3, fraction = 2, message = "{validation.digits}")
    private BigDecimal discount;

    @Column(nullable = false, precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.money.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.amount.digits}")
    private BigDecimal discountAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.money.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.amount.digits}")
    private BigDecimal totalPrice;// (количество*цена-скидка)

    @NotNull(message = "{validation.notNull}")
    @Column(nullable = false, precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.percentage.min}")
    @DecimalMax(value = "100.0", message = "{validation.percentage.max}")
    @Digits(integer = 3, fraction = 2, message = "{validation.digits}")
    private BigDecimal tax;

    @Column(nullable = false, precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.money.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.amount.digits}")
    private BigDecimal taxAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.money.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.amount.digits}")
    private BigDecimal totalAmount; // (количество*цена-скидка) + налог
}
