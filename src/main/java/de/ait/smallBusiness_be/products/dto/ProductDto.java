package de.ait.smallBusiness_be.products.dto;

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
    BigDecimal markupPercentage;
    BigDecimal sellingPrice;
    UnitOfMeasurementDto unitOfMeasurement;
    BigDecimal weight;
    NewDimensionsDto newDimensions;
    ProductCategoryDto productCategory;
    String description;
    String customsNumber;
    LocalDateTime createdDate;
    LocalDateTime dateOfLastPurchase;
    LocalDateTime lastModifiedDate;
    String storageLocation;
    Boolean hasPhoto;
    BigDecimal quantity;
}
