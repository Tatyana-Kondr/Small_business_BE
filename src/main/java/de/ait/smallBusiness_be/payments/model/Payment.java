package de.ait.smallBusiness_be.payments.model;

import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.purchases.model.Purchase;
import de.ait.smallBusiness_be.purchases.model.TypeOfDocument;
import de.ait.smallBusiness_be.sales.models.Sale;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
    private Long id;

    @Column(nullable = false)
    @NotNull(message = "{validation.notNull}")
    @PastOrPresent(message = "{validation.date.pastOrPresent}")
    private LocalDate paymentDate;

    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
    @NotNull(message = "{validation.notNull}")
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "{validation.notNull}")
    private PaymentType type;

    @Column(nullable = false, precision = 12, scale = 2)
    @NotNull(message = "{validation.notNull}")
    @DecimalMin(value = "0.01", message = "{validation.positive}")
    @Digits(integer = 10, fraction = 2, message = "{validation.amount.digits}")
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "sale_id")
    private Sale sale;

    @ManyToOne
    @JoinColumn(name = "purchase_id")
    private Purchase purchase;

    @ManyToOne
    @JoinColumn(name = "type_of_document_id", nullable = false)
    @NotNull(message = "{validation.notNull}")
    private TypeOfDocument document;

    @Column(nullable = false, length = 150)
    @NotBlank(message = "{validation.notBlank}")
    @Size(max = 150, message = "{validation.max.size}")
    private String documentNumber;


    @ManyToOne
    @JoinColumn(name = "payment_method_id", nullable = false)
    @NotNull(message = "{validation.notNull}")
    private PaymentMethod paymentMethod;

    @ManyToOne
    @JoinColumn(name = "payment_process_id", nullable = false)
    @NotNull(message = "{validation.notNull}")
    private PaymentProcess paymentProcess;
}
