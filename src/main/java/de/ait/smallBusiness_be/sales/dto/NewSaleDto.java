package de.ait.smallBusiness_be.sales.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
        @Schema(description = "Type of operation", example = "VERKAUF",allowableValues = {" VERKAUF, KUNDENERSTATTUNG, EXCHANGE"})
        String typeOfOperation;

        @Schema(description = "Shipping method", example = "DHL_PAKET",allowableValues = {"DHL_PAKET, POST_MAXI_BRIEF, HERMES, ASH_LOGISTIK_LUFTFRACHT_TRANSPORTE_ZOLLSERVICE, ABHOLUNG"})
        @Pattern(regexp = "DHL_PAKET|POST_MAXI_BRIEF|HERMES|ASH_LOGISTIK_LUFTFRACHT_TRANSPORTE_ZOLLSERVICE|ABHOLUNG")
        String shipping;

        @Schema(description = "Shipping dimensions (weight, width, height, length)")
        NewShippingDimensionsDto shippingDimensions;

        @Schema(description = "Terms of payment", example = "BETRAG_IM_BAR", allowableValues = {"BETRAG_IM_BAR, ÜBERWEISUNG_7_TAGE_2_PROZENT_14_TAGE_NETTO,\n" +
                "    ÜBERWEISUNG_7_TAGE, ÜBERWEISUNG_14_TAGE, BETRAG_ERHALTEN_AM"})
        @Pattern(regexp = "BETRAG_IM_BAR|ÜBERWEISUNG_7_TAGE_2_PROZENT_14_TAGE_NETTO|ÜBERWEISUNG_7_TAGE|ÜBERWEISUNG_14_TAGE|BETRAG_ERHALTEN_AM")
        String termsOfPayment;

        @PastOrPresent(message = "{validation.dateOfLastPurchase.pastOrPresent}")
        @Schema(description = "Date of the sale", example = "2025-02-05")
        LocalDate salesDate;

        @NotNull(message = "{validation.notNull}")
        @Pattern(regexp = "NICHT_BEZAHLT|TEILWEISEBEZAHLT|BEZAHLT|PENDING|CANCELLED")
        @Schema(description = "Payment status", example = "NICHT_BEZAHLT", allowableValues = {"NICHT_BEZAHLT, TEILWEISEBEZAHLT, BEZAHLT,  PENDING, CANCELLED"})
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

        List<NewSaleItemDto> salesItems;
}
