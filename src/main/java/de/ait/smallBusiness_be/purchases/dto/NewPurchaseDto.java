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

    @NotNull(message = "{validation.notNull}")
    @Schema(description = "Customer's id", example = "1")
    Long vendorId;

    @PastOrPresent(message = "{validation.dateOfLastPurchase.pastOrPresent}")
    @Schema(description = "Date of purchase", example = "2025-01-01")
    LocalDate purchasingDate;

    @NotNull(message = "{validation.notNull}")
    @Schema(description = "Type of operation", example = "EINKAUF, LIEFERANT_RABATT")
    String type;

    @Schema(description = "Type of purchasing document", example = "RECHNUNG, BON, BESTELLUNG")
    String document;

    @Schema(description = "Document's number", example = "12345-Aa")
    String documentNumber;

    @Schema(description = "Status of payment", example = "AUSSTEHEND")
    String paymentStatus;

    List<NewPurchaseItemDto> purchaseItems;
}
