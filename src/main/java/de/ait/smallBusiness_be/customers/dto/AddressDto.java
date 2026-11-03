package de.ait.smallBusiness_be.customers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import static de.ait.smallBusiness_be.constaints.EntityValidationConstants.*;
import static de.ait.smallBusiness_be.constaints.EntityValidationConstants.BUILDING_REGEX;

/**
 * @author Kondratyeva
 * @date 29.11.2024
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {

    @Length(min = 4, max = 10, message = "{validation.size}")
    @Pattern(regexp = POSTAL_CODE_REGEX, message = "{address.postalCode.Pattern.message}")
    @Schema(description = "Postal code", example = "60365")
    private String postalCode;

    @NotBlank(message = "{validation.notBlank}")
    @Schema(description = "Country name", example = "DE")
    private String country;

    @Length(min = 2, max = 50, message = "{validation.size}")
    @Pattern(regexp = CITY_REGEX, message = "{address.city.Pattern.message}")
    @Schema(description = "City name", example = "Frankfurt")
    private String city;

    @Length(min = 3, max = 100, message = "{validation.size}")
    @Pattern(regexp = STREET_REGEX, message = "{address.street.Pattern.message}")
    @Schema(description = "Street name", example = "Willy-Brandt-Platz")
    private String street;

    @Length(min = 1, max = 6, message = "{validation.size}")
    @Pattern(regexp = BUILDING_REGEX, message = "{address.building.Pattern.message}")
    @Schema(description = "Building number", example = "12")
    private String building;
}
