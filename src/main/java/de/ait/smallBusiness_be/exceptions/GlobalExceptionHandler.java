
package de.ait.smallBusiness_be.exceptions;

import de.ait.smallBusiness_be.validation.dto.ValidationErrorDto;
import de.ait.smallBusiness_be.validation.dto.ValidationErrorsDto;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // -------------------- REST API EXCEPTION --------------------

    @ExceptionHandler(RestApiException.class)
    public ResponseEntity<ErrorResponseDto> handleRestApiException(
            RestApiException ex
    ) {
        log.warn(
                "API error: status={}, message={}",
                ex.getStatus().value(),
                ex.getMessage()
        );

        return ResponseEntity
                .status(ex.getStatus())
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    // -------------------- FILE ERRORS --------------------

    @ExceptionHandler(UnsupportedFileTypeException.class)
    public ResponseEntity<ErrorResponseDto> handleUnsupportedFileTypeException(
            UnsupportedFileTypeException ex
    ) {
        log.warn("Unsupported file type: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(InvalidFileNameException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidFileNameException(
            InvalidFileNameException ex
    ) {
        log.warn("Invalid file name: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleFileNotFoundException(
            FileNotFoundException ex
    ) {
        log.warn("File not found: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    // -------------------- BEAN VALIDATION --------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorsDto> handleValidationException(
            MethodArgumentNotValidException ex
    ) {
        List<ValidationErrorDto> validationErrors =
                ex.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(fieldError -> ValidationErrorDto.builder()
                                .field(fieldError.getField())
                                .message(fieldError.getDefaultMessage())
                                .build()
                        )
                        .toList();

        log.warn(
                "Validation failed: {} field error(s)",
                validationErrors.size()
        );

        return ResponseEntity
                .badRequest()
                .body(ValidationErrorsDto.builder()
                        .errors(validationErrors)
                        .build());
    }

    // -------------------- FIELD VALIDATION --------------------

    @ExceptionHandler(FieldValidationException.class)
    public ResponseEntity<ValidationErrorsDto> handleFieldValidationException(
            FieldValidationException ex
    ) {
        log.warn("Field validation failed: {}", ex.getErrors());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ValidationErrorsDto.builder()
                        .errors(ex.getErrors())
                        .build());
    }

    // -------------------- DATABASE CONFLICT --------------------

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex
    ) {
        log.error("Database integrity violation", ex);

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDto(
                        ErrorDescription.DATA_INTEGRITY_VIOLATION
                                .getDescription()
                ));
    }

    // -------------------- INVALID ARGUMENT --------------------

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(
            IllegalArgumentException ex
    ) {
        log.warn("Invalid argument: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(
                        "Invalid request parameters"
                ));
    }

    // -------------------- ENTITY NOT FOUND --------------------

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleEntityNotFoundException(
            EntityNotFoundException ex
    ) {
        log.warn("Entity not found: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDto(
                        "Requested resource not found"
                ));
    }

    // -------------------- UNEXPECTED ERRORS --------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleUnexpectedException(
            Exception ex
    ) {
        log.error("Unexpected server error", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDto(
                        ErrorDescription.INTERNAL_SERVER_ERROR
                                .getDescription()
                ));
    }
}
