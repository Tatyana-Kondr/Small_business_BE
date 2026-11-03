package de.ait.smallBusiness_be.products.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.StringJoiner;


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

    @Column(nullable = false, length = 150)
    @NotBlank(message = "{validation.notBlank}")
    @Size(min = 3, max = 150, message = "{validation.name.size}")
    private String name;

    @Size(max = 30, message = "{validation.article.size}")
    @Column(name = "article", length = 30)
    private String article; // артикль который должен присваиваться автоматически или лучше сказать генерироваться, но также должна быть возможность его забивать вручную

    @Column(length = 30)
    @Size(max = 30, message = "{validation.max.size}")
    private String vendorArticle;  // артикль от поставщика

    @Column(precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.money.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.price.digits}")
    private BigDecimal purchasingPrice; // закупочная цена, по идее должна присваиваться цена с последней накладной, если ее значение превышает предыдущее

    @Column(precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.percentage.min}")
    @Digits(integer = 3, fraction = 2, message = "{validation.digits}")
    private BigDecimal markupPercentage; //наценка в %

    @Column(precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "{validation.money.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.price.digits}")
    private BigDecimal sellingPrice;  // продажная цена, по идее должна присваиваться цена с последней продажи. Если продаж еще не было, то цена формируется из закупочной цены + 20%

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unit_of_measurement_id", nullable = false)
    @NotNull(message = "{validation.notNull}")
    private UnitOfMeasurement unitOfMeasurement; // единица измерения

    @Column(precision = 13, scale = 3)
    @DecimalMin(value = "0.0", message = "{validation.weight.min}")
    @Digits(integer = 10, fraction = 3, message = "{validation.weight.digits}")
    private BigDecimal weight; // вес будет измерятся только в кг

    @Embedded
    private Dimensions dimensions; //размеры деталей

    @ManyToOne
    @JoinColumn(name = "product_category_id", referencedColumnName = "id", nullable = false)
    @NotNull(message = "{validation.notNull}")
    private ProductCategory productCategory; // категории продуктов, например электрика, механика, пневматика и т.д.

    @Column(length = 1024)
    @Size( max = 1024, message = "{validation.description.size}")
    private String description; // доп.информация

    @Column(length = 20)
    @Size(max = 20, message = "{validation.max.size}")
    private String customsNumber; // таможенный номер

    @Column
    @PastOrPresent(message = "{validation.date.pastOrPresent}")
    private LocalDateTime dateOfLastPurchase; // дата последней закупки/каталога

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    @Column(length = 30)
    @Size(max = 30, message = "{validation.max.size}")
    String storageLocation;


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
                .add("markup=" + markupPercentage)
                .add("sellingPrice=" + (sellingPrice != null ? sellingPrice.setScale(2,RoundingMode.HALF_UP) : "0.00"))
                .add("unitOfMeasurement='" + unitOfMeasurement + "'")
                .add("weight=" + weight + "'")
                .add("size='" + (dimensions != null ? dimensions : "N/A") + "'")
                .add("productCategory=" + (productCategory != null ? productCategory.getName() : "N/A"))
                .add("description='" + (description != null ? description : "N/A") + "'")
                .add("customsNumber='" + (customsNumber != null ? customsNumber : "N/A") + "'")
                .add("dateOfLastPurchase=" + (dateOfLastPurchase != null ? dateOfLastPurchase : "N/A"))
                .add("storageLocation='" + storageLocation + "'")
                .toString();
    }
}