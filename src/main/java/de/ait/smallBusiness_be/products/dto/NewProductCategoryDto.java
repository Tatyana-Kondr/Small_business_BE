package de.ait.smallBusiness_be.products.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import static de.ait.smallBusiness_be.constaints.EntityValidationConstants.NAME_REGEX;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NewProductCategoryDto{

    @NotBlank(message = "{validation.notBlank}")
    @Size(min = 3, max = 50, message = "{validation.name.size}")
    @Pattern(regexp = NAME_REGEX, message = "{name.Pattern.message}")
    @Schema(description = "Product category name", example = "ELEKTRIK")
    String name;

    @NotBlank(message = "{validation.notBlank}")
    @Size(min = 2, max = 5, message = "{validation.name.size}")
    @Pattern(regexp = NAME_REGEX, message = "{name.Pattern.message}")
    @Schema(description = "Product category article name", example = "EL")
    String artName;

}