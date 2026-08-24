package de.ait.smallBusiness_be.exceptions;

import de.ait.smallBusiness_be.validation.dto.ValidationErrorDto;
import lombok.Getter;

import java.util.List;

@Getter
public class FieldValidationException extends RuntimeException {

    private final List<ValidationErrorDto> errors;

    public FieldValidationException(List<ValidationErrorDto> errors) {
        super("Validation failed");
        this.errors = errors;
    }
}
