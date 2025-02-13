package de.ait.smallBusiness_be.productions.dto;

import de.ait.smallBusiness_be.productions.model.ProductionItem;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.purchases.model.TypeOfOperation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
@Schema(name = "Production", description = "Data of production")
public class ProductionDto {

    Long id;
    LocalDate dateOfProduction;
    String type;
    Long productId;
    Integer quantity;
    BigDecimal unitPrice;
    BigDecimal amount;
    List<ProductionItem> purchaseItems = new ArrayList<>();
}
