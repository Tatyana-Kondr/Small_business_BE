package de.ait.smallBusiness_be.payments.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "payment_methods")
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{validation.notBlank}")
    @Size(max = 50, message = "{validation.max.size}")
    @Column(nullable = false, length = 50)
    private String provider;

    @Size(max = 30, message = "{validation.max.size}")
    @Column(name = "masked_number", length = 30)
    private String maskedNumber;

    @Size(max = 50, message = "{validation.max.size}")
    @Column(length = 50)
    private String details;

    @Column(nullable = false)
    private boolean active;
}


