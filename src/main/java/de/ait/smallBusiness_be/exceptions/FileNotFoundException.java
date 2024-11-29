package de.ait.smallBusiness_be.exceptions;

/**
 * @author admin
 * @date 28.11.2024
 */
public class FileNotFoundException extends RuntimeException {
    public FileNotFoundException(Long id) {
        super(String.format("%s: %s", ErrorDescription.FILE_NOT_FOUND.getDescription(), "File-ID: " + id));
    }
}
