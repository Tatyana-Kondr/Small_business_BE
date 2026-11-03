package de.ait.smallBusiness_be.sales.models;

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
@Table(name = "terms_of_payment")
public class TermOfPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{validation.notBlank}")
    @Size(max = 50, message = "{validation.max.size}")
    @Column(name = "name", unique = true, nullable = false, length = 50)
    private String name;
}