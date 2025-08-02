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
    @Size(max = 50, message = "Provider must not exceed 50 characters")
    @Column(nullable = false)
    private String provider;  // Например, Visa, Mastercard, Альфа-Банк

    @Size(max = 30, message = "Masked number must not exceed 30 characters")
    @Column(name = "masked_number")
    private String maskedNumber;  // **** **** **** 1234 или банковский счёт

    @Size(max = 50, message = "Details must not exceed 50 characters")
    private String details;  // Например: срок действия, комментарий

    @Column(nullable = false)
    private boolean active;
}


