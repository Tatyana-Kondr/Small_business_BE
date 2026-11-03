package de.ait.smallBusiness_be.warehouse.models;

import de.ait.smallBusiness_be.products.model.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "warehouse")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull(message = "{validation.notNull}")
    @Column(precision = 9, scale = 3, nullable = false)
    @Digits(integer = 6, fraction = 3, message = "{validation.quantity.digits}")
    private BigDecimal quantity;
}

