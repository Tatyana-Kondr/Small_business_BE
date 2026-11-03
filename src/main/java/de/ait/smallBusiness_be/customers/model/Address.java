package de.ait.smallBusiness_be.customers.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import static de.ait.smallBusiness_be.constaints.EntityValidationConstants.*;

/**
 * @author admin
 * @date 28.11.2024
 */

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {

    @Column(name = "postal_code", length = 10)
    @Length(min = 4, max = 10, message = "{validation.size}")
    @Pattern(regexp = POSTAL_CODE_REGEX, message = "{address.postalCode.Pattern.message}")
    private String postalCode;

    @NotBlank(message = "{validation.notBlank}")
    private String country;

    @Column(length = 50)
    @Length(min = 3, max = 50, message = "{validation.size}")
    @Pattern(regexp = CITY_REGEX, message = "{address.city.Pattern.message}")
    private String city;

    @Column(length = 100)
    @Length(min = 3, max = 100, message = "{validation.size}")
    @Pattern(regexp = STREET_REGEX, message = "{address.street.Pattern.message}")
    private String street;

    @Column(length = 20)
    @Length(min = 1, max = 20, message = "{validation.size}")
    @Pattern(regexp = BUILDING_REGEX, message = "{address.building.Pattern.message}")
    private String building;
}

