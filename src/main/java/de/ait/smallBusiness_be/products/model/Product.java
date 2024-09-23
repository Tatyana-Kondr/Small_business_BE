package de.ait.smallBusiness_be.products.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

import static de.ait.smallBusiness_be.constants.EntityValidationConstants.DESCRIPTION_REGEX;
import static de.ait.smallBusiness_be.constants.EntityValidationConstants.NAME_REGEX;

/**
 * 19.09.2024
 * MashCom_BE
 *
 * @author Kondratyeva (AIT TR)
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "validation.notBlank")
    @Size(min = 3, max = 100, message = "{validation.name.size}")
    @Pattern(regexp = NAME_REGEX, message = "{name.Pattern.message}")
    private String name;

    @Column(nullable = false)
    @Size(max = 30, message = "{validation.max.size}")
    private String article;

    @Column(nullable = false)
    @Size(max = 30, message = "{validation.max.size}")
    private String vendorArticle;

    @Column(nullable = true)
    @DecimalMin(value = "0.0", inclusive = true, message = "{validation.price.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.price.digits}")
    private BigDecimal purchasingPrice;

    @Column(nullable = true)
    @DecimalMin(value = "0.0", inclusive = true, message = "{validation.price.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.price.digits}")
    private BigDecimal sellingPrice;

    @Column(nullable = false)
    @NotBlank(message = "{validation.notBlank}")
    private String unitOfMeasurement; // единица измерения

    @Column(nullable = true)
    @DecimalMin(value = "0.0", inclusive = true, message = "{validation.weight.min}")
    @Digits(integer = 5, fraction = 3, message = "{validation.weight.digits}")
    private Float weight; // вес будет измерятся только в кг

    @Column(nullable = true)
    @Size(max = 50, message = "{validation.max.size}")
    private String size;

    @ManyToOne
    @JoinColumn(name = "productCategory_id", referencedColumnName = "id", nullable = false)
    @NotNull(message = "{validation.notNull}")
    private ProductCategory productCategory;

    @NotBlank(message = "{validation.notBlank}" )
    @Size(min = 3, max = 1024, message = "{validation.description.size}")
    @Pattern(regexp = DESCRIPTION_REGEX, message = "{description.Pattern.message}")
    private String description;

    @Column(nullable = true)
    @Size(max = 20, message = "{validation.max.size}")
    private String customsNumber;

    @Column(nullable = true)
    @PastOrPresent(message = "{validation.dateOfLastPurchase.pastOrPresent}")
    private Date dateOfLastPurchase;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("Product: ID - %d, name - %s, article - %s, vendor_Article - %s, purchasing_Price - %.2f, date Of Last Purchase - %s," +
                " selling_Price - %.2f, unit Of Measurement - %s, weight - %.3f, size -%s, product_Category - %s," +
                " description - %s, customs_Number - %s",
                id,
                name,
                article != null ? article : "N/A",  // Проверка на null для article
                vendorArticle != null ? vendorArticle : "N/A",  // Проверка на null для vendor_Article
                purchasingPrice != null ? purchasingPrice.setScale(2).toPlainString() : "0.00",  // Округление для BigDecimal
                dateOfLastPurchase != null ? dateOfLastPurchase.toString() : "N/A",  // Проверка на null для даты
                sellingPrice != null ? sellingPrice.setScale(2).toPlainString() : "0.00",  // Округление для BigDecimal
                unitOfMeasurement,
                weight != null ? weight : 0.0f,  // Проверка на null для weight
                size != null ? size : "N/A",  // Проверка на null для size
                productCategory != null ? productCategory.getName() : "N/A",  // Проверка на null для категории
                description != null ? description : "N/A",  // Проверка на null для description
                customsNumber != null ? customsNumber : "N/A");  // Проверка на null для customsNumber
    }
}
