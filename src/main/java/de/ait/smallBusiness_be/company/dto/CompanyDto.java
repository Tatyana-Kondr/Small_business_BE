package de.ait.smallBusiness_be.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "Company", description = "Data of company")
public class CompanyDto {
    Long id;
    String name;
    String address;
    String phone;
    String email;
    String bank;
    String ibanNumber;
    String bicNumber;
    String vatId;
    String logoUrl;
}
