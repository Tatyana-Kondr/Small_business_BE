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
        @Size(min = 1, max = 50, message = "{validation.name.size}")
        @Schema(description = "Product's article", example = "BL-1")
        String article;

        @Size( max = 50, message = "{validation.name.size}")
        @Schema(description = "Product's vendor article", example = "ab123")
        String vendorArticle;

        @DecimalMin(value = "0.0", message = "{validation.price.min}")
        @Digits(integer = 10, fraction = 2, message = "{validation.price.digits}")
        @Schema(description = "Product's purchasing price", example = "20.0")
        BigDecimal purchasingPrice;

        @DecimalMin(value = "0.0", message = "{validation.tax.min}")
        @Schema(description = "Markup percentage", example = "20")
        BigDecimal markupPercentage;

        @DecimalMin(value = "0.0", message = "{validation.price.min}")
        @Digits(integer = 10, fraction = 2, message = "{validation.price.digits}")
        @Schema(description = "Product's selling price", example = "22.0")
        BigDecimal sellingPrice;

        @Schema(description = "Unit Of Measurement", example = "ST")
        Long unitOfMeasurementId;

        @DecimalMin(value = "0.0", message = "{validation.weight.min}")
        @Digits(integer = 5, fraction = 3, message = "{validation.weight.digits}")
        BigDecimal weight;

        @Schema(description = "Product's dimensions")
        NewDimensionsDto newDimensions;

        @NotNull(message = "{validation.notNull}")
        @Schema(description = "Product's category", example = "Electronics")
        ProductCategory productCategory;

        @Size( max = 1024, message = "{validation.description.size}")
        @Schema(description = "Product's description", example = "A new phone, perfect for your child")
        String description;

        @Size(max = 20, message = "{validation.max.size}")
        @Schema(description = "Product's custom number", example = "534455")
        String customsNumber;

        @Size(max = 30, message = "{validation.max.size}")
        @Schema(description = "Product storage location", example = "L1, p-11")
        String storageLocation;
//        @PastOrPresent(message = "{validation.dateOfLastPurchase.pastOrPresent}")
//        LocalDateTime dateOfLastPurchase;
}
