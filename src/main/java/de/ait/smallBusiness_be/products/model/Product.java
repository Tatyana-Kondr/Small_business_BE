package de.ait.smallBusiness_be.products.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

import static de.ait.smallBusiness_be.constaints.EntityValidationConstants.DESCRIPTION_REGEX;
import static de.ait.smallBusiness_be.constaints.EntityValidationConstants.NAME_REGEX;

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
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "validation.notBlank")
    @Size(min = 3, max = 100, message = "{validation.name.size}")
    @Pattern(regexp = NAME_REGEX, message = "{name.Pattern.message}")
    private String name;   // наименование продукта

    @Column(unique = true)
    @Size(max = 30, message = "{validation.max.size}")
    private String article; // артикль который должен присваиваться автоматически или лучше сказать генерироваться, но также должна быть возможность его забивать вручную

    @Column
    @Size(max = 30, message = "{validation.max.size}")
    private String vendorArticle;  // артикль от поставщика

    @Column
    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.price.digits}")
    private BigDecimal purchasingPrice; // закупочная цена, по идее должна присваиваться цена с последней накладной, если ее значение превышает предыдущее

    @Column
    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.price.digits}")
    private BigDecimal sellingPrice;  // продажная цена, по идее должна присваиваться цена с последней продажи. Если продаж еще не было, то цена формируется из закупочной цены + 20%

    @Enumerated(EnumType.STRING)
    @Column//(nullable = false)
    //@NotNull(message = "{validation.notNull}")
    private UnitOfMeasurement unitOfMeasurement; // единица измерения

    @Column(precision = 8, scale = 3)
    @DecimalMin(value = "0.0", message = "{validation.weight.min}")
    @Digits(integer = 5, fraction = 3, message = "{validation.weight.digits}")
    private BigDecimal weight; // вес будет измерятся только в кг

    @Embedded
    private Dimensions dimensions; //размеры деталей

    @ManyToOne
    @JoinColumn(name = "product_category_id", referencedColumnName = "id", nullable = false)
    @NotNull(message = "{validation.notNull}")
    private ProductCategory productCategory; // категории продуктов, например электрика, механика, пневматика и т.д.

    @Size(min = 3, max = 1024, message = "{validation.description.size}")
    @Pattern(regexp = DESCRIPTION_REGEX, message = "{description.Pattern.message}")
    private String description; // доп.информация

    @Column
    @Size(max = 20, message = "{validation.max.size}")
    private String customsNumber; // таможенный номер

    @Column
    @PastOrPresent(message = "{validation.dateOfLastPurchase.pastOrPresent}")
    private LocalDateTime dateOfLastPurchase; // дата последней закупки/каталога

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate = LocalDateTime.now();// под вопросом

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

//    @PostPersist
//    public void postPersist() {
//        generateArticle();
//    }

    @PrePersist
    @PreUpdate
    public void onSaveOrUpdate() {
        calculateSellingPrice();
    }


    public void calculateSellingPrice() {
        if (this.sellingPrice == null) {
            if (this.purchasingPrice != null && this.purchasingPrice.compareTo(BigDecimal.ZERO) > 0) {

                this.sellingPrice = this.purchasingPrice
                        .multiply(BigDecimal.valueOf(1.2))
                        .setScale(2, RoundingMode.HALF_UP);
            } else {

                this.sellingPrice = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }
        }
    }

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
        return new StringJoiner(", ", Product.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("name='" + name + "'")
                .add("article='" + (article != null ? article : "N/A") + "'")
                .add("vendorArticle='" + (vendorArticle != null ? vendorArticle : "N/A") + "'")
                .add("purchasingPrice=" + (purchasingPrice != null ? purchasingPrice.setScale(2,RoundingMode.HALF_UP) : "0.00"))
                .add("sellingPrice=" + (sellingPrice != null ? sellingPrice.setScale(2,RoundingMode.HALF_UP) : "0.00"))
                .add("unitOfMeasurement='" + unitOfMeasurement + "'")
                .add("weight=" + (weight != null ? weight : 0.0f))
                .add("size='" + (dimensions != null ? dimensions : "N/A") + "'")
                .add("productCategory=" + (productCategory != null ? productCategory.getName() : "N/A"))
                .add("description='" + (description != null ? description : "N/A") + "'")
                .add("customsNumber='" + (customsNumber != null ? customsNumber : "N/A") + "'")
                .add("dateOfLastPurchase=" + (dateOfLastPurchase != null ? dateOfLastPurchase : "N/A"))
                .toString();
    }
}