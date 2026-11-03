package de.ait.smallBusiness_be.products.model;

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
@Table(name = "units_of_measurement")
public class UnitOfMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{validation.notBlank}")
    @Size(max = 20, message = "{validation.max.size}")
    @Column(nullable = false, unique = true, length = 20)
    private String name;

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UnitOfMeasurement that)) return false;

        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format(
                "UnitOfMeasurement: id - %s, name - %s",
                id,
                name
        );
    }
}
