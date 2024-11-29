package de.ait.smallBusiness_be.customers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;

import static de.ait.smallBusiness_be.constaints.EntityValidationConstants.*;
import static de.ait.smallBusiness_be.constaints.EntityValidationConstants.BUILDING_REGEX;

/**
 * @author Kondratyeva
 * @date 29.11.2024
 */

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {

    @Length(min = 4, max = 10, message = "{javax.validation.constraints.Size.message}")
    @Pattern(regexp = POSTAL_CODE_REGEX, message = "{address.postalCode.Pattern.message}")
    @Schema(description = "Postal code", example = "60365")
    String postalCode;

    @NotBlank(message = "{javax.validation.constraints.NotBlank.message}")
    @Schema(description = "Country name", example = "DE")
    String country;

    @Length(min = 3, max = 50, message = "{javax.validation.constraints.Size.message}")
    @Pattern(regexp = CITY_REGEX, message = "{address.city.Pattern.message}")
    @Schema(description = "City name", example = "Frankfurt")
    String city;

    @Length(min = 3, max = 100, message = "{javax.validation.constraints.Size.message}")
    @Pattern(regexp = STREET_REGEX, message = "{address.street.Pattern.message}")
    @Schema(description = "Street name", example = "Willy-Brandt-Platz")
    String street;

    @Length(min = 1, max = 6, message = "{javax.validation.constraints.Size.message}")
    @Pattern(regexp = BUILDING_REGEX, message = "{address.building.Pattern.message}")
    @Schema(description = "Building number", example = "12")
    String building;
}
