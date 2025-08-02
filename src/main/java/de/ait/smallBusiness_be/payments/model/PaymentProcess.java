package de.ait.smallBusiness_be.payments.model;

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
@Table(name = "payment_processes")
public class PaymentProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{validation.notBlank}")
    @Column(name = "process_name", unique = true, nullable = false)
    @Schema(description = "Name of the payment process (e.g. 'Bank Transfer', 'Card payment', 'PayPal')", example = "Kartenzahlung")
    private String processName;
}

