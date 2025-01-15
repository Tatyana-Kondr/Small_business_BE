package de.ait.smallBusiness_be.purchases.dto;

import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.purchases.model.PurchaseItem;
import de.ait.smallBusiness_be.purchases.model.TypeOfDocument;
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
 * 15.01.2025
 * SmB
 *
 * @author Kondratyeva (AIT TR)
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "New Purchase", description = "Data for registration of new purchases")
public class NewPurchaseDto {

    @NotBlank(message = "{validation.notBlank}")
    @Schema(description = "Customer's id", example = "1")
    Long vendorId;

    @PastOrPresent(message = "{validation.dateOfLastPurchase.pastOrPresent}")
    @Schema(description = "Date of purchase", example = "01-01-2025")
    LocalDate purchasingDate;

    @Schema(description = "Type of operation", example = "EINKAUF, LIEFERANT_RABATT, VERKAUF, KUNDENERSTATTUNG")
    String type;

    @Schema(description = "Type of purchasing document", example = "RECHNUNG, BON, BESTELLUNG")
    String document;

    @Schema(description = "Document's number", example = "12345-Aa")
    String documentNumber;

    @DecimalMin(value = "0", message = "{validation.tax.min}")
    @NotNull(message = "{validation.notNull}")
    @Schema(description = "Tax percentage", example = "19")
    BigDecimal tax; // НДС в процентах

    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
    @Schema(description = "Amount without percentage", example = "123.00")
    BigDecimal subtotal; // Подитог

    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
    @NotNull(message = "{validation.notNull}")
    @Schema(description = "Amount tax", example = "23.37")
    BigDecimal taxSum; // Сумма налога

    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
    @NotNull(message = "{validation.notNull}")
    @Schema(description = "Total amount", example = "146.37")
    BigDecimal total; // Общая сумма

    List<PurchaseItem> purchaseItems = new ArrayList<>(); // может лучше сделать отдельное ДТО?
}
