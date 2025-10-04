package de.ait.smallBusiness_be.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static de.ait.smallBusiness_be.constaints.EntityValidationConstants.IBAN_DE_REGEX;
import static de.ait.smallBusiness_be.constaints.EntityValidationConstants.PHONE_REGEX;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "New Company", description = "Registration data")
public class NewCompanyDto {

    @NotBlank(message = "validation.notBlank")
    @Size(min = 3, max = 100, message = "{validation.name.size}")
    @Schema(description = "Company name", example = "Muster")
    String name;

    @NotBlank(message = "validation.notBlank")
    @Schema(description = "Company address", example = "MusterWeg 1, 12345 Berlin, DE")
    String address;

    @Pattern(regexp = PHONE_REGEX, message = "{phone.Pattern.message}")
    @Nullable
    @Schema(description = "Company phone number", example = "+4917654875612")
    String phone;

    @Email
    @NotBlank(message = "validation.notBlank")
    @Schema(description = "Company email address", example = "company@gmail.com")
    String email;

    @NotBlank(message = "validation.notBlank")
    @Schema(description = "Company bank name", example = "MusterBank ")
    String bank;

    @NotBlank(message = "validation.notBlank")
    @Pattern(regexp = IBAN_DE_REGEX, message = "{iban.Pattern.message}")
    @Schema(description = "Company IBAN", example = "DE89370400440532013000")
    String ibanNumber;

    @NotBlank(message = "validation.notBlank")
    @Size(min = 8, max = 11, message = "{validation.swift.size}")
    @Schema(description = "Company bank swift number", example = "DEUTDEFF")
    String bicNumber;

    @NotBlank(message = "validation.notBlank")
    @Pattern(regexp = "^DE[0-9]{9}$", message = "{vat.Pattern.message}")
    @Schema(description = "Company value added tax identification number", example = "DE123456789")
    String vatId;
}
