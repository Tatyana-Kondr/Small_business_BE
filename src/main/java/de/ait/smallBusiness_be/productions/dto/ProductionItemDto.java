package de.ait.smallBusiness_be.productions.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 13.02.2025
 * SmB_be
 *
 * @author Kondratyeva (AIT TR)
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "ProductionItem", description = "Data of productionItem")
public class ProductionItemDto {

    Long id;
    Long productionId;
    Long productId;
    String type;
    BigDecimal quantity;
    BigDecimal unitPrice;
    BigDecimal totalPrice;
}
