package de.ait.smallBusiness_be.purchases.model;

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
@Table(name = "types_of_document")
public class TypeOfDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{validation.notBlank}")
    @Size(max = 30, message = "{validation.max.size}")
    @Column(name = "name", unique = true, nullable = false, length = 30)
    private String name;
}
