package de.ait.smallBusiness_be.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseStockDto {

    private Long productId;
    private String productName;
    private String productArticle;
    private BigDecimal quantity;
}
