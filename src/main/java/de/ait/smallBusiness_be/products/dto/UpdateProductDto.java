package de.ait.smallBusiness_be.products.dto;

import de.ait.smallBusiness_be.products.model.ProductCategory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

import static de.ait.smallBusiness_be.constaints.EntityValidationConstants.DESCRIPTION_REGEX;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductDto{

        @NotBlank(message = "{validation.notBlank}")
        @Size(min = 3, max = 100, message = "{validation.name.size}")
        String name;

        //@NotBlank(message = "{validation.notBlank}")
        @Size(min = 3, max = 50, message = "{validation.name.size}")
        String article;

        @Size(min = 3, max = 50, message = "{validation.name.size}")
        String vendorArticle;

        @DecimalMin(value = "0.0", message = "{validation.price.min}")
        @Digits(integer = 10, fraction = 2, message = "{validation.price.digits}")
        BigDecimal purchasingPrice;

        @DecimalMin(value = "0.0", message = "{validation.price.min}")
        @Digits(integer = 10, fraction = 2, message = "{validation.price.digits}")
        BigDecimal sellingPrice;

        @NotBlank(message = "{validation.notBlank}")
        String unitOfMeasurement;

        @DecimalMin(value = "0.0", message = "{validation.weight.min}")
        @Digits(integer = 5, fraction = 3, message = "{validation.weight.digits}")
        BigDecimal weight;

        NewDimensionsDto newDimensions;

        @NotNull(message = "{validation.notNull}")
        ProductCategory productCategory;

        @Size(min = 3, max = 1024, message = "{validation.description.size}")
        @Pattern(regexp = DESCRIPTION_REGEX, message = "{description.Pattern.message}")
        String description;

        @Size(max = 20, message = "{validation.max.size}")
        String customsNumber;
//
//        @PastOrPresent(message = "{validation.dateOfLastPurchase.pastOrPresent}")
//        LocalDateTime dateOfLastPurchase;
}
