package de.ait.smallBusiness_be.warehouse.models;

import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.purchases.model.TypeOfOperation;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "warehouse_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @NotNull(message = "{validation.notNull}")
    @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "{validation.notNull}")
    @Column(nullable = false, length = 30)
    private TypeOfOperation typeOfOperation;

    @NotNull(message = "{validation.notNull}")
    @Column(nullable = false)
    private Long documentId;

    @NotNull(message = "{validation.notNull}")
    @PastOrPresent(message = "{validation.date.pastOrPresent}")
    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer partner;

    @Column(name = "partner_name")
    private String partnerName;

    @NotNull(message = "{validation.notNull}")
    @Column(precision = 9, scale = 3, nullable = false)
    @DecimalMin(value = "0.001", message = "{validation.positive}")
    @Digits(integer = 6, fraction = 3, message = "{validation.quantity.digits}")
    private BigDecimal quantity;

}

