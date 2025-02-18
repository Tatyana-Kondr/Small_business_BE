package de.ait.smallBusiness_be.sales.models;

import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.purchases.model.PaymentStatus;
import de.ait.smallBusiness_be.purchases.model.TypeOfOperation;
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
@Table(name = "Sales")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
    @NotNull(message = "{validation.notNull}")
    private Customer customer;

    @Column(nullable = false)
    private String invoiceNumber; // номер счета (начинается с года, а дальше подряд- пример: 2025-001)

    @Column
    private String accountObject; // в некоторых счетах необходимо указывать объекты

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TypeOfOperation typeOfOperation;

    @Column
    @Enumerated(EnumType.STRING)
    private Shipping shipping;

    @Embedded
    private ShippingDimensions shippingDimensions;

    @Column
    @Enumerated(EnumType.STRING)
    private TermsOfPayment termsOfPayment;

    @Column
    @PastOrPresent
    private LocalDate salesDate;

    @Column
    @PastOrPresent
    private LocalDate paymentDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column(precision = 8, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
    private BigDecimal discountAmount;

    @Column(precision = 8, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
    private BigDecimal totalPrice;// (количество*цена-скидка)

    @Column(precision = 8, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
    private BigDecimal taxAmount;

    @Column(precision = 8, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
    private BigDecimal totalAmount; // (количество*цена-скидка) + налог

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SaleItem> salesItems = new ArrayList<>();
}
