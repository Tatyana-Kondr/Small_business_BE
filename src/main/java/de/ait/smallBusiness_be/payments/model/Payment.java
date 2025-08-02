package de.ait.smallBusiness_be.payments.model;

import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.purchases.model.Purchase;
import de.ait.smallBusiness_be.purchases.model.TypeOfDocument;
import de.ait.smallBusiness_be.sales.models.Sale;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column
    @PastOrPresent(message = "{validation.dateOfLastPurchase.pastOrPresent}")
    LocalDate paymentDate;

    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
    @NotNull(message = "{validation.notNull}")
    Customer customer;

    @Column(nullable = false)
    PaymentType type;

    @Column(precision = 8, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
    BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "sale_id")
    Sale sale;

    @ManyToOne
    @JoinColumn(name = "purchase_id")
    Purchase purchase;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    TypeOfDocument document;

    @Column(nullable = false)
    String documentNumber;

    @ManyToOne
    @JoinColumn(name = "paymentMethod_id", nullable = false)
    PaymentMethod paymentMethod;

    @ManyToOne
    @JoinColumn(name = "paymentProcess_id")
    PaymentProcess paymentProcess;
}
