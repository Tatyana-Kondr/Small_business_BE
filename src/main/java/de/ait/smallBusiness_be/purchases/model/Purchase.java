package de.ait.smallBusiness_be.purchases.model;

import de.ait.smallBusiness_be.customers.model.Customer;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
@Table(name = "purchases")
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
    @NotNull(message = "{validation.notNull}")
    Customer vendor;

    @Column
    @PastOrPresent(message = "{validation.dateOfLastPurchase.pastOrPresent}")
    LocalDate purchasingDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    TypeOfOperation type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    TypeOfDocument document;

    @Column(nullable = false)
    String documentNumber;

    @Column(precision = 8, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
    BigDecimal subtotal; // Подитог

    @Column(precision = 8, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
    BigDecimal taxSum; // Сумма налога

    @Column(precision = 8, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
    BigDecimal total; // Общая сумма

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    PaymentStatus paymentStatus;

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<PurchaseItem> purchaseItems = new ArrayList<>(); // Список позиций


}
