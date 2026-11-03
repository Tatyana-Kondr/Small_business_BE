package de.ait.smallBusiness_be.products.dto;

import de.ait.smallBusiness_be.products.model.ProductCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
/**
 * 11/25/2024
 * Small_business_BE
 *
 * @author Chechkina (AIT TR)
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "New Product", description = "Information for creating a product")
public class NewProductDto{

    @NotBlank(message = "{validation.notBlank}")
    @Size(min = 3, max = 150, message = "{validation.name.size}")
    @Schema(description = "Product's name", example = "Lampe")
    String name;

    @Size( max = 30, message = "{validation.article.size}")
    @Schema(description = "Product's vendor article", example = "ab123")
    String vendorArticle;

    @DecimalMin(value = "0.0", message = "{validation.money.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.price.digits}")
    @Schema(description = "Product's purchasing price", example = "20.0")
    BigDecimal purchasingPrice;

    @DecimalMin(value = "0.0", message = "{validation.percentage.min}")
    @Digits(integer = 3, fraction = 2, message = "{validation.digits}")
    @Schema(description = "Markup percentage", example = "20.00")
    BigDecimal markupPercentage;

    @DecimalMin(value = "0.0", message = "{validation.money.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.price.digits}")
    @Schema(description = "Product's purchasing price", example = "20.0")
    BigDecimal sellingPrice;

    @NotNull(message = "{validation.notNull}")
    @Positive(message = "{validation.positive}")
    @Schema(description = "Product category ID", example = "1")
    Long productCategoryId;

    @NotNull(message = "{validation.notNull}")
    @Positive(message = "{validation.positive}")
    @Schema(description = "Unit Of Measurement", example = "ST")
    Long unitOfMeasurementId;

    @Size(max = 30, message = "{validation.max.size}")
    @Schema(description = "Product storage location", example = "L1, p-11")
    String storageLocation;
}
