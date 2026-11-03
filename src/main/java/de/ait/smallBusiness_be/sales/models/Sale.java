package de.ait.smallBusiness_be.sales.models;

import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.purchases.model.PaymentStatus;
import de.ait.smallBusiness_be.purchases.model.TypeOfOperation;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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

    @Column(unique = true, nullable = false)
    private String invoiceNumber; // номер счета (начинается с года, а дальше подряд- пример: 2025-001)

    @Column
    private String accountObject; // в некоторых счетах необходимо указывать объекты

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TypeOfOperation typeOfOperation;

    @ManyToOne
    @JoinColumn(name = "shipping_id")
    private Shipping shipping;

    @Embedded
    private ShippingDimensions shippingDimensions;

    @ManyToOne(optional = false)
    @JoinColumn(name = "term_of_payment_id", nullable = false)
    @NotNull(message = "{validation.notNull}")
    private TermOfPayment termsOfPayment;

    @Column(nullable = false)
    @NotNull(message = "{validation.notNull}")
    @PastOrPresent(message = "{validation.date.pastOrPresent}")
    private LocalDate salesDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column
    @PastOrPresent(message = "{validation.date.pastOrPresent}")
    private LocalDate paymentDate;

    @Column
    private String orderNumber;

    @Column
    private String orderType;

    @Column
    private LocalDate deliveryDate;

    @Column
    private String deliveryBill;

    @Column(nullable = false, precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.money.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.amount.digits}")
    private BigDecimal discountAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.money.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.amount.digits}")
    private BigDecimal totalPrice;// (количество*цена-скидка)

    @Column(nullable = false, precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.money.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.amount.digits}")
    private BigDecimal taxAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.money.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.amount.digits}")
    private BigDecimal totalAmount; // (количество*цена-скидка) + налог

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SaleItem> saleItems = new ArrayList<>();
}
