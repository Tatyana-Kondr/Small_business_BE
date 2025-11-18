package de.ait.smallBusiness_be.warehouse.dto;

import de.ait.smallBusiness_be.purchases.model.TypeOfOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseRecordDto {

    private Long id;
    private TypeOfOperation typeOfOperation;
    private Long documentId;
    private LocalDate date;
    private String partnerName;
    private BigDecimal quantity;
    private Long productId;
    private String productName;
}
