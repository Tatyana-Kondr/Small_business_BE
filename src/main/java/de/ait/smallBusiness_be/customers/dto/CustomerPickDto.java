package de.ait.smallBusiness_be.customers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "CustomerPick", description = "Data of customer")
public class CustomerPickDto {
    Long id;

    String name;

    String customerNumber;
}
