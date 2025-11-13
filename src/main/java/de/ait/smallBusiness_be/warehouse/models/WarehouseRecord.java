package de.ait.smallBusiness_be.warehouse.models;

import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.purchases.model.TypeOfOperation;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
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
    @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
    private Product product;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TypeOfOperation typeOfOperation;

    @Column(nullable = false)
    private Long documentId;

    @PastOrPresent(message = "{validation.dateOfLastPurchase.pastOrPresent}")
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer partner;

    @Column(name = "partner_name")
    private String partnerName;

    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 6, fraction = 3, message = "{validation.price.digits}")
    private BigDecimal quantity;

}

