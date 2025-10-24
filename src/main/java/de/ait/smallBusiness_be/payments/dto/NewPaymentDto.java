package de.ait.smallBusiness_be.payments.dto;

import de.ait.smallBusiness_be.payments.model.PaymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "NewPaymentDto", description = "Data for creating a new payment")
public class NewPaymentDto {

    @NotNull(message = "{validation.notNull}")
    @PastOrPresent(message = "{validation.dateOfLastPurchase.pastOrPresent}")
    @Schema(description = "Payment date", example = "2025-07-15", required = true)
    private LocalDate paymentDate;

    @NotNull(message = "{validation.notNull}")
    @Schema(description = "ID of the customer", example = "1", required = true)
    private Long customerId;

    @NotNull(message = "{validation.notNull}")
    @Schema(description = "Type of payment (EINNAHME for sales, AUSGABE for purchases)", example = "EINNAHME", required = true)
    private PaymentType type;

    @NotNull(message = "{validation.notNull}")
    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.price.digits}")
    @Schema(description = "Payment amount", example = "1250.50", required = true)
    private BigDecimal amount;

    @Schema(description = "Sale ID if payment relates to a sale", example = "10")
    private Long saleId;

    @Schema(description = "Purchase ID if payment relates to a purchase", example = "5")
    private Long purchaseId;

    @NotNull(message = "{validation.notNull}")
    @Schema(description = "Document type", example = "RECHNUNG")
    private Long documentId;

    @NotBlank(message = "{validation.notBlank}")
    @Schema(description = "Document number", example = "RE-2025-056")
    private String documentNumber;

    @NotNull(message = "{validation.notNull}")
    @Schema(description = "ID of the payment method (e.g., Visa, Bank Transfer)", example = "1", required = true)
    private Long paymentMethodId;

    @NotNull(message = "{validation.notNull}")
    @Schema(description = "ID of the payment process (e.g., reference to 'Bank Transfer')", example = "2", required = true)
    private Long paymentProcessId;
}

