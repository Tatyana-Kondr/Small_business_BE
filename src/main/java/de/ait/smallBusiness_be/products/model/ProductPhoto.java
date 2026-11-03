package de.ait.smallBusiness_be.products.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;


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
@Table(name = "product_photos")
public class ProductPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "{validation.notNull}")
    private Product product;

    private String originFileName;

    private String fileUrl;

    @Column(name = "position")
    private Integer position;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductPhoto that)) return false;

        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format(
                "ProductPhoto: id - %d, productId - %s, position - %s",
                id,
                product != null ? product.getId() : null,
                position
        );
    }
}
