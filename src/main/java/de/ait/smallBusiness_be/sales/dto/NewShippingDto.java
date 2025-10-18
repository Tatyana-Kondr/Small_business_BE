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
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "NewShipping", description = "Data for creating a new shipping")
public class NewShippingDto {

    @NotBlank(message = "{validation.notBlank}")
    @Size(max = 50, message = "Shipping name must not exceed 50 characters")
    @Schema(description = "Name of the shipping", example = "Hermes", required = true)
    private String name;
}
