package de.ait.smallBusiness_be.products.dto;

import de.ait.smallBusiness_be.products.model.ProductCategory;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "Update Product", description = "Information for updating a product")
public class UpdateProductDto{

        @NotBlank(message = "{validation.notBlank}")
        @Size(min = 3, max = 100, message = "{validation.name.size}")
        @Schema(description = "Product's name", example = "Lampe")
        String name;

        @NotBlank(message = "{validation.notBlank}")
        @Size(min = 3, max = 50, message = "{validation.name.size}")
        @Schema(description = "Product's article", example = "BL-1")
        String article;

        @Size(min = 3, max = 50, message = "{validation.name.size}")
        @Schema(description = "Product's vendor article", example = "ab123")
        String vendorArticle;

        @DecimalMin(value = "0.0", message = "{validation.price.min}")
        @Digits(integer = 10, fraction = 2, message = "{validation.price.digits}")
        @Schema(description = "Product's purchasing price", example = "20.0")
        BigDecimal purchasingPrice;

        @DecimalMin(value = "0.0", message = "{validation.price.min}")
        @Digits(integer = 10, fraction = 2, message = "{validation.price.digits}")
        @Schema(description = "Product's selling price", example = "22.0")
        BigDecimal sellingPrice;

        @NotBlank(message = "{validation.notBlank}")
        @Pattern(regexp = "KG|PIECE|LITER|METER|ST")
        @Schema(description = "Unit Of Measurement",
                example = "ST",
                allowableValues = {"KG, PIECE, LITER, METER, ST"})
        String unitOfMeasurement;

        @DecimalMin(value = "0.0", message = "{validation.weight.min}")
        @Digits(integer = 5, fraction = 3, message = "{validation.weight.digits}")
        BigDecimal weight;

        @Schema(description = "Product's dimensions")
        NewDimensionsDto newDimensions;

        @NotNull(message = "{validation.notNull}")
        @Schema(description = "Product's category", example = "Electronics")
        ProductCategory productCategory;

        @Size(min = 3, max = 1024, message = "{validation.description.size}")
        @Pattern(regexp = DESCRIPTION_REGEX, message = "{description.Pattern.message}")
        @Schema(description = "Product's description", example = "A new phone, perfect for your child")
        String description;

        @Size(max = 20, message = "{validation.max.size}")
        @Schema(description = "Product's custom number", example = "534455")
        String customsNumber;
//
//        @PastOrPresent(message = "{validation.dateOfLastPurchase.pastOrPresent}")
//        LocalDateTime dateOfLastPurchase;
}
