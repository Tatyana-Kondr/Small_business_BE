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

    @Column(nullable = false)
    @NotNull(message = "{validation.notNull}")
    @PastOrPresent(message = "{validation.date.pastOrPresent}")
    LocalDate purchasingDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    TypeOfOperation type;

    @ManyToOne
    @JoinColumn(name = "type_of_document_id")
    @NotNull(message = "{validation.notNull}")
    TypeOfDocument document;

    String documentNumber;

    @Column(nullable = false, precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.money.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.amount.digits}")
    BigDecimal subtotal; // Подитог

    @Column(nullable = false, precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.money.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.amount.digits}")
    BigDecimal discountAmount; // Общая сумма скидок по позициям

    @Column(nullable = false, precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.money.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.amount.digits}")
    BigDecimal taxSum; // Сумма налога

    @Column(nullable = false, precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.money.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.amount.digits}")
    BigDecimal total; // Общая сумма

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    PaymentStatus paymentStatus;

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    List<PurchaseItem> purchaseItems = new ArrayList<>();
}
