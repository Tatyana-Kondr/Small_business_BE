package de.ait.smallBusiness_be.products.dto;


import de.ait.smallBusiness_be.products.model.ProductCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SmallBusiness_BE
 * 24.10.2024
 *
 * @author Kondratyeva
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto{

    Long id;
    String name;
    String article;
    String vendorArticle;
    BigDecimal purchasingPrice;
    BigDecimal sellingPrice;
    String unitOfMeasurement;
    BigDecimal weight;
    NewDimensionsDto newDimensions;
    ProductCategory productCategory;
    String description;
    String customsNumber;
    LocalDateTime createdDate;
    LocalDateTime dateOfLastPurchase;
    LocalDateTime lastModifiedDate;
}
