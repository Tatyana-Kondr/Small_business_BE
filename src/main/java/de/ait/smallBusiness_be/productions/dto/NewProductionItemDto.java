package de.ait.smallBusiness_be.productions.dto;

import de.ait.smallBusiness_be.products.model.Product;
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
 * 12.02.2025
 * SmB_be
 *
 * @author Kondratyeva (AIT TR)
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "New ProductionItem", description = "Data for registration of new productionItem")
public class NewProductionItemDto {

    @NotNull(message = "{validation.notNull}")
    @Schema(description = "Production's id", example = "1")
    Long productionId;

    @NotNull(message = "{validation.notNull}")
    @Schema(description = "Product's id", example = "1")
    Long productId;

    @Schema(description = "Type of operation", example = "PRODUKTIONSMATERIAL")
    String type;

    @NotNull(message = "{validation.notNull}")
    @Schema(description = "Quantity", example = "1")
    Integer quantity;

    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
    @Schema(description = "Unit price", example = "5.00")
    BigDecimal unitPrice;

    @NotNull(message = "{validation.notNull}")
    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
    @Schema(description = "Amount without percentage", example = "0")
    BigDecimal totalPrice;
}
