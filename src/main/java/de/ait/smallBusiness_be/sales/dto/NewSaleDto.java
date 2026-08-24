package de.ait.smallBusiness_be.sales.dto;

import de.ait.smallBusiness_be.purchases.model.PaymentStatus;
import de.ait.smallBusiness_be.purchases.model.TypeOfOperation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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

        @NotNull(message = "{validation.notNull}")
        @Positive(message = "{validation.positive}")
        @Schema(description = "Customer ID", example = "10")
        Long customerId;

        @Schema(description = "Invoice number")
        String invoiceNumber;

        @Size(max = 100, message = "{javax.validation.constraints.Size.message}")
        @Schema(description = "Account object (optional)", example = "Project Alpha")
        String accountObject;

        @NotNull(message = "{validation.notNull}")
        @Schema(description = "Type of operation", example = "VERKAUF")
        TypeOfOperation typeOfOperation;

        @Positive(message = "{validation.positive}")
        @Schema(description = "ID of the shipping (e.g., Hermes, Post)", example = "1")
        Long shippingId;

        @Valid
        @Schema(description = "Shipping dimensions (weight, width, height, length)")
        NewShippingDimensionsDto shippingDimensions;

        @NotNull(message = "{validation.notNull}")
        @Positive(message = "{validation.positive}")
        @Schema(description = "Terms of payment", example = "Betrag in Bar")
        Long termsOfPaymentId;

        @NotNull(message = "{validation.notNull}")
        @PastOrPresent(message = "{validation.date.pastOrPresent}")
        @Schema(description = "Date of the sale", example = "2025-02-05")
        LocalDate salesDate;

        @NotNull(message = "{validation.notNull}")
        @Schema(description = "Payment status", example = "OFFEN")
        PaymentStatus paymentStatus;

        @PastOrPresent(message = "{validation.dateOfLastPurchase.pastOrPresent}")
        @Schema(description = "Date of the payment", example = "2025-02-01")
        LocalDate paymentDate;

        @Size(max = 50, message = "{javax.validation.constraints.Size.message}")
        @Schema(description = "Order number", example = "12345")
        String orderNumber;

        @Size(max = 100, message = "{javax.validation.constraints.Size.message}")
        @Schema(description = "Order type", example = "A.Muller")
        String orderType;

        @Schema(description = "Date of the delivery", example = "2025-02-05")
        LocalDate deliveryDate;

        @Schema(description = "Delivery bill number")
        String deliveryBill;

        @NotNull(message = "{validation.notNull}")
        @DecimalMin(value = "0.0", inclusive = true, message = "{validation.percentage.min}")
        @DecimalMax(value = "100.0", inclusive = true, message = "{validation.percentage.max}")
        @Schema(description = "Default tax percentage")
        BigDecimal defaultTax;

        @NotNull(message = "{validation.notNull}")
        @DecimalMin(value = "0.0", inclusive = true, message = "{validation.percentage.min}")
        @DecimalMax(value = "100.0", inclusive = true, message = "{validation.percentage.max}")
        @Schema(description = "Default discount percentage")
        BigDecimal defaultDiscount;

        @Valid
        @NotEmpty(message = "{validation.sale.items.notEmpty}")
        List<NewSaleItemDto> salesItems;
}
