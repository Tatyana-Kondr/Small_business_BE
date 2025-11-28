package de.ait.smallBusiness_be.sales.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "New Sale", description = "Information for creating a new sale")
public class NewSaleDto {

        @Schema(description = "Customer ID", example = "10")
        Long customerId;

        @Schema(description = "Invoice number")
        String invoiceNumber;

        @Schema(description = "Account object (optional)", example = "Project Alpha")
        String accountObject;

        @NotNull(message = "{validation.notNull}")
        @Pattern(regexp = "VERKAUF|KUNDENERSTATTUNG")
        @Schema(description = "Type of operation", example = "VERKAUF",allowableValues = {" VERKAUF, KUNDENERSTATTUNG"})
        String typeOfOperation;

        @Schema(description = "ID of the shipping (e.g., Hermes, Post)", example = "1", required = true)
        Long shippingId;

        @Schema(description = "Shipping dimensions (weight, width, height, length)")
        NewShippingDimensionsDto shippingDimensions;

        @Schema(description = "Terms of payment", example = "Betrag in Bar")
        Long termsOfPaymentId;

        @PastOrPresent(message = "{validation.dateOfLastPurchase.pastOrPresent}")
        @Schema(description = "Date of the sale", example = "2025-02-05")
        LocalDate salesDate;

        @NotNull(message = "{validation.notNull}")
        @Pattern(regexp = "AUSSTEHEND|ANZAHLUNG|BEZAHLT|CANCELLED")
        @Schema(description = "Payment status", example = "AUSSTEHEND", allowableValues = {"AUSSTEHEND, ANZAHLUNG, BEZAHLT, CANCELLED"})
        String paymentStatus;

        @PastOrPresent(message = "{validation.dateOfLastPurchase.pastOrPresent}")
        @Schema(description = "Date of the payment", example = "2025-02-01")
        private LocalDate paymentDate;

        @Size(max = 50, message = "{javax.validation.constraints.Size.message}")
        @Schema(description = "Order number", example = "12345")
        private String orderNumber;

        @Schema(description = "Order type", example = "A.Muller")
        private String orderType;

        @Schema(description = "Date of the delivery", example = "2025-02-05")
        private LocalDate deliveryDate;

        @Schema(description = "Delivery bill number")
        private String deliveryBill;

        @Schema(description = "Default tax percentage")
        private BigDecimal defaultTax;

        @Schema(description = "Default discount percentage")
        private BigDecimal defaultDiscount;

        List<NewSaleItemDto> salesItems;
}
