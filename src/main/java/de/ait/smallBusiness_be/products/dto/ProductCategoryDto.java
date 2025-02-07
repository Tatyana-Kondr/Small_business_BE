package de.ait.smallBusiness_be.products.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SmallBusiness_BE
 * 24.10.2024
 *
 * @author Kondratyeva
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Product Category")
public class ProductCategoryDto{
        @Schema(description = "ID of the product category", example = "1")
        Integer id;

        @Schema(description = "Name of the product category", example = "Electronics")
        String name;

        @Schema(description = "Vendor article name of the product category", example = "ELC")
        String artName;
}
