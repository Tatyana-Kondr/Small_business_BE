package de.ait.smallBusiness_be.sales.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(name = "New Sale", description = "Information for creating a new sale")
public record NewSaleDto(

        @Schema(description = "Customer ID", example = "12345")
        Long customerId,

        @NotBlank(message = "{validation.notBlank}")
        @Size(min = 5, max = 50, message = "{javax.validation.constraints.Size.message}")
        @Schema(description = "Invoice number", example = "INV-2024-001")
        String invoiceNumber,

        @Schema(description = "Account object (optional)", example = "Project Alpha")
        String accountObject,

        @NotNull(message = "{validation.notNull}")
        @Pattern(regexp = "EINKAUF|LIEFERANT_RABATT|VERKAUF|KUNDENERSTATTUNG|EXCHANGE")
        @Schema(description = "Type of operation", example = "VERKAUF",allowableValues = {"EINKAUF, LIEFERANT_RABATT, VERKAUF, KUNDENERSTATTUNG, EXCHANGE"})
        String typeOfOperation,

        @Schema(description = "Shipping method", example = "Courier")
        String shipping,

        @Schema(description = "Shipping dimensions (weight, width, height, length)")
        NewShippingDimensionsDto shippingDimensions,

        @Schema(description = "Terms of payment", example = "Net 30")
        String termsOfPayment,

        @PastOrPresent
        @Schema(description = "Date of the sale", example = "2024-02-05")
        LocalDate salesDate,

        @PastOrPresent
        @Schema(description = "Date of payment", example = "2024-02-10")
        LocalDate paymentDate,

        @NotNull(message = "{validation.notNull}")
        @Pattern(regexp = "NICHT_BEZAHLT|TEILWEISEBEZAHLT|BEZAHLT|PENDING|CANCELLED")
        @Schema(description = "Payment status", example = "NICHT_BEZAHLT", allowableValues = {"NICHT_BEZAHLT, TEILWEISEBEZAHLT, BEZAHLT,  PENDING, CANCELLED"})
        String paymentStatus,

        @DecimalMin(value = "0", message = "{validation.tax.min}")
        @Schema(description = "Discount amount", example = "00.00")
        BigDecimal discountAmount,

        @DecimalMin(value = "0.0", message = "{validation.price.min}")
        @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
        @Schema(description = "Total price before tax", example = "00.00")
        BigDecimal totalPrice,

        @DecimalMin(value = "0.0", message = "{validation.price.min}")
        @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
        @Schema(description = "Tax amount", example = "00.00")
        BigDecimal taxAmount,

        @DecimalMin(value = "0.0", message = "{validation.price.min}")
        @Digits(integer = 6, fraction = 2, message = "{validation.price.digits}")
        @Schema(description = "Total amount after tax", example = "00.00")
        BigDecimal totalAmount,

        List<NewSaleItemDto> salesItems) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
