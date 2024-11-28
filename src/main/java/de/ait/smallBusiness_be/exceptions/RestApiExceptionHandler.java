package de.ait.smallBusiness_be.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestApiExceptionHandler {

    @ExceptionHandler(RestApiException.class)
    public ResponseEntity<String> handleRestApiException(RestApiException e) {
        return ResponseEntity.status(e.getStatus()).body(e.getMessage());
    }
}