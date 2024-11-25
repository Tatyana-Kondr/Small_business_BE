package de.ait.smallBusiness_be.products.dto;


import de.ait.smallBusiness_be.products.model.ProductCategory;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * SmallBusiness_BE
 * 24.10.2024
 *
 * @author Kondratyeva
 */

public record ProductDto(
        Long id,
        String name,
        String article,
        String vendorArticle,
        BigDecimal purchasingPrice,
        BigDecimal sellingPrice,
        String unitOfMeasurement,
        Float weight,
        String size,
        ProductCategory productCategory,
        String description,
        String customsNumber,
        Date dateOfLastPurchase) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
