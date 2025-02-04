package de.ait.smallBusiness_be.validation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
@Schema(name = "ValidationErrors", description = "Information about validation errors")
public class ValidationErrorsDto {

    @Schema(description = "List of validation errors")
    private List<ValidationErrorDto> errors;
}
