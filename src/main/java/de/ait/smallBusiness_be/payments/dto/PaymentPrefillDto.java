package de.ait.smallBusiness_be.payments.dto;

import de.ait.smallBusiness_be.payments.model.PaymentType;
import de.ait.smallBusiness_be.purchases.model.TypeOfDocument;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "PaymentPrefillDto", description = "Prefilled data for creating a new payment")
public class PaymentPrefillDto {

    @Schema(description = "ID of the customer", example = "1")
    private Long customerId;

    @Schema(description = "Name of the customer", example = "Max Mustermann")
    private String customerName;

    @Schema(description = "Amount to be paid", example = "250.00")
    private BigDecimal amount;

    @Schema(description = "Left amount after partial payment", example = "100.00")
    private BigDecimal amountLeft;

    @Schema(description = "Related sale ID", example = "10")
    private Long saleId;

    @Schema(description = "Related purchase ID", example = "5")
    private Long purchaseId;

    @Schema(description = "Document type", example = "RECHNUNG")
    private TypeOfDocument document;

    @Schema(description = "Document number", example = "RE-2025-001")
    private String documentNumber;

    @Schema(description = "Payment type (EINNAHME/AUSGABE)", example = "EINNAHME")
    private PaymentType type;
}

