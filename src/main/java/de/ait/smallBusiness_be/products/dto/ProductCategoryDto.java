package de.ait.smallBusiness_be.products.dto;


import java.io.Serial;
import java.io.Serializable;

/**
 * SmallBusiness_BE
 * 24.10.2024
 *
 * @author Kondratyeva
 */

public record ProductCategoryDto(
        int id,
        String name) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
