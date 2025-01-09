package de.ait.smallBusiness_be.customers.dto;

import de.ait.smallBusiness_be.customers.model.Address;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import static de.ait.smallBusiness_be.constaints.EntityValidationConstants.*;

/**
 * @author admin
 * @date 28.11.2024
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "New Customer", description = "Data for registration of new customers")
public class NewCustomerDto {

    @NotBlank(message = "validation.notBlank")
    @Size(min = 3, max = 100, message = "{validation.name.size}")
    @Schema(description = "Customer's name", example = "Amazon")
    String name;

    @Size(min = 3, max = 6, message = "{validation.name.size}")
    @Schema(description = "Customer's number", example = "12345")
    String customerNumber;

    AddressDto addressDto;

    @Pattern(regexp = PHONE_REGEX, message = "{phone.Pattern.message}")
    @Schema(description = "The customer's phone number", example = "+4917654875612")
    String phone;

    @Pattern(regexp = EMAIL_REGEX, message = "{email.Pattern.message}")
    @Schema(description = "The customer's e-mail", example = "amazon@mail.com")
    String email;

    @Pattern(regexp = WEBSITE_REGEX, message = "{website.Pattern.message}")
    @Schema(description = "The customer's website", example = "amazon.com")
    String website;
}
