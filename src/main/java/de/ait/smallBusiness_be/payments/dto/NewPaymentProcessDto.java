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
@Schema(name = "NewPaymentProcess", description = "Data for creating a new payment process")
public class NewPaymentProcessDto {

    @NotBlank(message = "{validation.notBlank}")
    @Size(max = 30, message = "Process name must not exceed 30 characters")
    @Schema(description = "Name of the payment process (e.g., 'Bank Transfer', 'Kartenzahlung')", example = "Kartenzahlung", required = true)
    private String processName;
}
