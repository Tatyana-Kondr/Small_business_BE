package de.ait.smallBusiness_be.sales.models;

import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.products.model.Dimensions;
import de.ait.smallBusiness_be.purchases.model.PaymentStatus;
import de.ait.smallBusiness_be.purchases.model.TypeOfOperation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;

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
    private String invoiceNumber;

    @Column
    private String accountObject; // в некоторых счетах необходимо указывать объекты

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TypeOfOperation typeOfOperation;

    @Column
    private String shipping;  // доставка, здесь будут фиксированные значения

    @Embedded
    private Dimensions shippingDimensions; //габариты посылки (вес, размер)

    @Column
    private String termsOfPayment; // условия оплаты????

    @Column
    @PastOrPresent
    private LocalDate salesDate;

    @Column
    @PastOrPresent
    private LocalDate paymentDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SaleItem> salesItems = new ArrayList<>();
}
