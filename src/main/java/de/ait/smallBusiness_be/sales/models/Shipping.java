package de.ait.smallBusiness_be.sales.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "shippings")
public class Shipping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotBlank(message = "{validation.notBlank}")
    @Column(name = "shipping_name", unique = true, nullable = false)
    @Schema(description = "Name of the shipping", example = "Hermes")
    String name;

}
