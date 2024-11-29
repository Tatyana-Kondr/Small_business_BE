package de.ait.smallBusiness_be.customers.dto;

import de.ait.smallBusiness_be.customers.model.Address;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * @author admin
 * @date 28.11.2024
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "Customer", description = "Data of customer")
public class CustomerDto {

    Long id;

    String name;

    String customerNumber;

    AddressDto addressDto;

    String phone;

    String email;

    String website;
}
