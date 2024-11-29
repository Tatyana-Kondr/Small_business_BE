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

    //@Column
    @Length(min = 4, max = 10, message = "{javax.validation.constraints.Size.message}")
    @Pattern(regexp = POSTAL_CODE_REGEX, message = "{address.postalCode.Pattern.message}")
    private String postalCode;

   // @Column
    @NotBlank(message = "{javax.validation.constraints.NotBlank.message}")
    private String country;

    //@Column
    @Length(min = 3, max = 50, message = "{javax.validation.constraints.Size.message}")
    @Pattern(regexp = CITY_REGEX, message = "{address.city.Pattern.message}")
    private String city;

    //@Column
    @Length(min = 3, max = 100, message = "{javax.validation.constraints.Size.message}")
    @Pattern(regexp = STREET_REGEX, message = "{address.street.Pattern.message}")
    private String street;

   //@Column
    @Length(min = 1, max = 6, message = "{javax.validation.constraints.Size.message}")
    @Pattern(regexp = BUILDING_REGEX, message = "{address.building.Pattern.message}")
    private String building;
}

