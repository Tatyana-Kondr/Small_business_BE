package de.ait.smallBusiness_be.sales.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "NewTermOfPayment", description = "Data for creating a new term of payment")
public class NewTermOfPaymentDto {
    @NotBlank(message = "{validation.notBlank}")
    @Size(max = 30, message = "Name must not exceed 30 characters")
    @Schema(description = "Name of the term of payment", example = "Betrag in Bar", required = true)
    String name;
}
