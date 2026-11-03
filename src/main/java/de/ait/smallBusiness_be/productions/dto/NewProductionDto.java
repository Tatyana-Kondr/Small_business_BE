package de.ait.smallBusiness_be.productions.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
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

    @NotNull(message = "{validation.notNull}")
    @PastOrPresent(message = "{validation.date.pastOrPresent}")
    @Schema(description = "Date of production", example = "2025-01-01")
    LocalDate dateOfProduction;

    @NotNull(message = "{validation.notNull}")
    @Schema(description = "Product's id", example = "3")
    Long productId;

    @NotNull(message = "{validation.notNull}")
    @DecimalMin(value = "0.001", message = "{validation.positive}")
    @Digits(integer = 6, fraction = 3, message = "{validation.quantity.digits}")
    @Schema(description = "Quantity", example = "1")
    BigDecimal quantity;

    @NotNull(message = "{validation.notNull}")
    @DecimalMin(value = "0.0", message = "{validation.money.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.price.digits}")
    @Schema(description = "Price", example = "10")
    BigDecimal unitPrice;

    @NotEmpty(message = "{validation.notEmpty}")
    @Valid
    List<NewProductionItemDto> productionItems = new ArrayList<>();
}
