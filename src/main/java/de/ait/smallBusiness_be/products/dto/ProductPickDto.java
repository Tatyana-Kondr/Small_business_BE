package de.ait.smallBusiness_be.products.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductPickDto {
    Long id;
    String name;
    String article;
    String vendorArticle;
    BigDecimal purchasingPrice;
    BigDecimal sellingPrice;
    Long categoryId;

}
