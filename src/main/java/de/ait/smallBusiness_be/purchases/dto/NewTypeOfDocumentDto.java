package de.ait.smallBusiness_be.purchases.dto;

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
@Schema(name = "NewTypeOfDocument", description = "Data for creating a new type of document")
public class NewTypeOfDocumentDto {
    @NotBlank(message = "{validation.notBlank}")
    @Size(max = 30, message = "Name must not exceed 30 characters")
    @Schema(description = "Name of the type of document", example = "RECHNUNG", required = true)
    String name;
}
