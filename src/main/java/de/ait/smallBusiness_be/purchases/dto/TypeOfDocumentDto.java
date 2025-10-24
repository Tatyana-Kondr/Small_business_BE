package de.ait.smallBusiness_be.purchases.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "TypeOfDocument", description = "Data of type of document")
public class TypeOfDocumentDto {
    Long id;
    String name;
}
