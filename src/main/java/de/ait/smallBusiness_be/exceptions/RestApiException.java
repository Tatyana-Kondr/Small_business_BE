package de.ait.smallBusiness_be.exceptions;


import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * SmallBusiness_BE
 * 24.10.2024
 *
 * @author Kondratyeva
 */

@Getter
public class RestApiException extends RuntimeException{

    private final HttpStatus status;

    public RestApiException(ErrorDescription errorDescription, HttpStatus status) {
        super(errorDescription.getDescription());
        this.status = status;
    }

    public RestApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public RestApiException(ErrorDescription errorDescription) {
        super(errorDescription.getDescription());
        this.status = HttpStatus.BAD_REQUEST;
    }

}
