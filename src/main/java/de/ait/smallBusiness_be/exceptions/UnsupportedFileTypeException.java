package de.ait.smallBusiness_be.exceptions;

/**
 * @author admin
 * @date 28.11.2024
 */
public class UnsupportedFileTypeException extends RuntimeException {

    public UnsupportedFileTypeException(String fileName) {
        super(String.format("%s: %s", ErrorDescription.UNSUPPORTED_FILE_TYPE.getDescription(), fileName));
    }
}