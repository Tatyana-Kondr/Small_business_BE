package de.ait.smallBusiness_be.validation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 1/27/2025
 * Small_business_BE
 *
 * @author Chechkina (AIT TR)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "ValidationError", description = "Description of the validation error")
public class ValidationErrorDto {

    @Schema(description = "The name of the field where the error occurred", example = "password")
    private String field;
    @Schema(description = "The validation error message", example = "Invalid password")
    private String message;
}
