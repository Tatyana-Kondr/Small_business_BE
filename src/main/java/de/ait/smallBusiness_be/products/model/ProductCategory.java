package de.ait.smallBusiness_be.products.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Objects;

import static de.ait.smallBusiness_be.constants.EntityValidationConstants.NAME_REGEX;

/**
 * 19.09.2024
 * MashCom_BE
 *
 * @author Kondratyeva (AIT TR)
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "product_categories")
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    @NotBlank(message = "{validation.notBlank}")
    @Size(min = 3, max = 50, message = "{validation.name.size}")
    @Pattern(regexp = NAME_REGEX, message = "{name.Pattern.message}")
    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductCategory that = (ProductCategory) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("ProductCategory: id - %d, name - %s", id, name);
    }
}

