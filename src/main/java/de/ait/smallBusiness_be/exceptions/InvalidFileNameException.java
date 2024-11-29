package de.ait.smallBusiness_be.exceptions;

/**
 * @author admin
 * @date 28.11.2024
 */
public class InvalidFileNameException extends RuntimeException {

    public InvalidFileNameException(String fileName) {
        super(String.format("%s: %s", ErrorDescription.INVALID_FILE_NAME.getDescription(), fileName));
    }
}