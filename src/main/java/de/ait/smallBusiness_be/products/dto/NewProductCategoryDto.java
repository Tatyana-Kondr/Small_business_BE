package de.ait.smallBusiness_be.products.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serial;
import java.io.Serializable;

import static de.ait.smallBusiness_be.constaints.EntityValidationConstants.NAME_REGEX;

public record NewProductCategoryDto(
        @NotBlank(message = "{validation.notBlank}")
        @Size(min = 3, max = 50, message = "{validation.name.size}")
        @Pattern(regexp = NAME_REGEX, message = "{name.Pattern.message}")
        String name) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
