package de.ait.smallBusiness_be.products.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
public class ProductCategoryDto{
        Integer id;
        String name;
        String artName;
}
