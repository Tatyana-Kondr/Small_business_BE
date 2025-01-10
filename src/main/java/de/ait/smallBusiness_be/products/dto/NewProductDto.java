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
public class NewProductDto{

    @NotBlank(message = "{validation.notBlank}")
    @Size(min = 3, max = 100, message = "{validation.name.size}")
    @Schema(description = "Product's name", example = "Lampe")
    String name;

    @Size(min = 3, max = 50, message = "{validation.name.size}")
    @Schema(description = "Product's vendor article", example = "ab123")
    String vendorArticle;

    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.price.digits}")
    @Schema(description = "Product's purchasing price", example = "20.0")
    BigDecimal purchasingPrice;

    @NotNull(message = "{validation.notNull}")
    ProductCategory productCategory;
}
