package de.ait.smallBusiness_be.products.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductPhotoOrderDto {

    private List<Long> photoIds;
}
