package de.ait.smallBusiness_be.exceptions;

import de.ait.smallBusiness_be.validation.dto.ValidationErrorDto;
import de.ait.smallBusiness_be.validation.dto.ValidationErrorsDto;
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

    @ExceptionHandler(RestApiException.class)
    public ResponseEntity<ErrorResponseDto> handleRestApiException(RestApiException ex) {
        log.error("An error occurred: ", ex);
        return ResponseEntity
                .status(ex.getStatus().value())
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(UnsupportedFileTypeException.class)
    public ResponseEntity<ErrorResponseDto> handleUnsupportedFileTypeException(UnsupportedFileTypeException ex) {
        log.error("Unsupported file type error: ", ex);
        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(InvalidFileNameException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidFileNameException(InvalidFileNameException ex) {
        log.error("Invalid file name error: ", ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleFileNotFoundException(FileNotFoundException ex) {
        log.error("File not found: ", ex);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorsDto> handleValidationException(
            MethodArgumentNotValidException ex
    ) {
        List<ValidationErrorDto> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> ValidationErrorDto.builder()
                        .field(fieldError.getField())
                        .rejectedValue(
                                fieldError.getRejectedValue() == null
                                        ? null
                                        : fieldError.getRejectedValue().toString()
                        )
                        .message(fieldError.getDefaultMessage())
                        .build()
                )
                .toList();

        log.warn("Validation errors: {}", validationErrors);

        return ResponseEntity
                .badRequest()
                .body(ValidationErrorsDto.builder()
                        .errors(validationErrors)
                        .build());
    }

    @ExceptionHandler(FieldValidationException.class)
    public ResponseEntity<ValidationErrorsDto> handleFieldValidationException(
            FieldValidationException ex
    ) {
        log.warn("Field validation errors: {}", ex.getErrors());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ValidationErrorsDto.builder()
                        .errors(ex.getErrors())
                        .build());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        ErrorResponseDto response = new ErrorResponseDto("Cannot be deleted because it is used in other entries.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}