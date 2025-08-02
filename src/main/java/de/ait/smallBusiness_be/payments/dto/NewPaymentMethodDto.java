package de.ait.smallBusiness_be.payments.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "NewPaymentMethodDto", description = "Data for registering a new payment method")
public class NewPaymentMethodDto {

    @NotBlank(message = "{validation.notBlank}")
    @Schema(description = "Payment provider name (e.g., bank or card type)", example = "PostBank VISA", required = true)
    private String provider;

    @Schema(description = "Masked card or account number", example = "**** **** **** 1234")
    private String maskedNumber;

    @Size(max = 50, message = "Details must not exceed 50 characters")
    @Schema(description = "Additional information (e.g., expiry date)", example = "02/29")
    private String details;

    @Schema(description = "Whether the method is active", example = "true", defaultValue = "true")
    private boolean active;
}

