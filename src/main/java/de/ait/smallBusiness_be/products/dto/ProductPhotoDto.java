package de.ait.smallBusiness_be.products.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductPhotoDto {

    private Long id;
    private String originFileName;
    private String fileUrl;
    private Integer position;
}
