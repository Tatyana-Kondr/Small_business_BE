package de.ait.smallBusiness_be.products.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Objects;

import static de.ait.smallBusiness_be.constaints.EntityValidationConstants.ART_NAME_REGEX;
import static de.ait.smallBusiness_be.constaints.EntityValidationConstants.NAME_REGEX;

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
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    @NotBlank(message = "{validation.notBlank}")
    @Size(min = 3, max = 50, message = "{validation.name.size}")
    @Pattern(regexp = NAME_REGEX, message = "{name.Pattern.message}")
    private String name;

    @Column(name = "art_name", nullable = false, unique = true, length = 6)
    @NotBlank(message = "{validation.notBlank}")
    @Size(min = 2, max = 6, message = "{validation.name.size}")
    @Pattern(regexp = ART_NAME_REGEX, message = "{validation.artName.pattern}")
    private String artName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductCategory that)) return false;

        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("ProductCategory: id - %d, name - %s", id, name);
    }
}

