package de.ait.smallBusiness_be.products.dto;

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
@Schema(name = "NewUnitOfMeasurement", description = "Data for creating a new unit of measurement")
public class NewUnitOfMeasurementDto {

    @NotBlank(message = "{validation.notBlank}")
    @Size(max = 20, message = "Name must not exceed 20 characters")
    @Schema(description = "Name of the unit of measurement", example = "ST", required = true)
    String name;
}
