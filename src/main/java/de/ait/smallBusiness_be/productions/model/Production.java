package de.ait.smallBusiness_be.productions.model;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import de.ait.smallBusiness_be.products.model.Product;
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
 * 12.02.2025
 * SmB_be
 *
 * @author Kondratyeva (AIT TR)
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "production")
public class Production {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    @PastOrPresent(message = "{validation.date.pastOrPresent}")
    @NotNull(message = "{validation.notNull}")
    LocalDate dateOfProduction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    TypeOfOperation type;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
    @NotNull(message = "{validation.notNull}")
    Product product;

    @NotNull(message = "{validation.notNull}")
    @Column(precision = 9, scale = 3, nullable = false)
    @DecimalMin(value = "0.001", message = "{validation.positive}")
    @Digits(integer = 6, fraction = 3, message = "{validation.quantity.digits}")
    BigDecimal quantity;

    @NotNull(message = "{validation.notNull}")
    @Column(precision =12, scale = 2, nullable = false)
    @DecimalMin(value = "0.0", message = "{validation.money.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.price.digits}")
    BigDecimal unitPrice;

    @NotNull(message = "{validation.notNull}")
    @Column(precision = 12, scale = 2, nullable = false)
    @DecimalMin(value = "0.01", message = "{validation.positive}")
    @Digits(integer = 10, fraction = 2, message = "{validation.amount.digits}")
    BigDecimal amount;

    @OneToMany(mappedBy = "production", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonManagedReference
    List<ProductionItem> productionItems = new ArrayList<>();
}
