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
@Schema(name = "Shipping", description = "Data of shipping")
public class ShippingDto {

    Long id;

    @NotBlank(message = "{validation.notBlank}")
    @Size(max = 50, message = "Shipping name must not exceed 50 characters")
    private String name;
}
