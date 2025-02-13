package de.ait.smallBusiness_be.productions.dto;

import de.ait.smallBusiness_be.productions.model.ProductionItem;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.purchases.model.TypeOfOperation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
@Schema(name = "New Production", description = "Data for registration of new production")
public class NewProductionDto {

    @PastOrPresent(message = "{validation.dateOfLastPurchase.pastOrPresent}")
    @Schema(description = "Date of production", example = "2025-01-01")
    LocalDate dateOfProduction;

    @Schema(description = "Type of operation", example = "PRODUKTION")
    String type;

    @NotBlank(message = "{validation.notBlank}")
    @Schema(description = "Product's id", example = "3")
    Long productId;

    @NotNull(message = "{validation.notNull}")
    @Schema(description = "Quantity", example = "1")
    Integer quantity;

    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
    @Schema(description = "Unit price", example = "10.00")
    BigDecimal unitPrice;

    @NotNull(message = "{validation.notNull}")
    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
    @Schema(description = "Amount", example = "0")
    BigDecimal amount;

    List<ProductionItem> purchaseItems = new ArrayList<>();
}
