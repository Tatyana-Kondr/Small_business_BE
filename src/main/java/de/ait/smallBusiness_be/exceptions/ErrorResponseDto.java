package de.ait.smallBusiness_be.exceptions;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author admin
 * @date 28.11.2024
 */
public record ErrorResponseDto(String message) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}