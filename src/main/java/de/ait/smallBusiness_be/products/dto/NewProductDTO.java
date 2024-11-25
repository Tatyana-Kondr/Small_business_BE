package de.ait.smallBusiness_be.products.dto;

import de.ait.smallBusiness_be.products.model.ProductCategory;
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
    String name;

    @Size(min = 3, max = 50, message = "{validation.name.size}")
    String vendorArticle;

    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.price.digits}")
    BigDecimal purchasingPrice;

    @NotNull(message = "{validation.notNull}")
    ProductCategory productCategory;
}
