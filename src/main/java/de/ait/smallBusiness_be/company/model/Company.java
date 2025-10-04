package de.ait.smallBusiness_be.company.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static de.ait.smallBusiness_be.constaints.EntityValidationConstants.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column
    @NotBlank(message = "validation.notBlank")
    @Size(min = 3, max = 100, message = "{validation.name.size}")
    String name;

    @Column
    @NotBlank(message = "validation.notBlank")
    String address;

    @Column
    @Pattern(regexp = PHONE_REGEX, message = "{phone.Pattern.message}")
    String phone;

    @Column
    @Pattern(regexp = EMAIL_REGEX, message = "{email.Pattern.message}")
    @NotBlank(message = "validation.notBlank")
    String email;

    @Column
    @NotBlank(message = "validation.notBlank")
    String bank;

    @Column
    @Pattern(regexp = IBAN_DE_REGEX, message = "{iban.Pattern.message}")
    @NotBlank(message = "validation.notBlank")
    String ibanNumber;

    @Column
    @Size(min = 8, max = 11, message = "{validation.swift.size}")
    @NotBlank(message = "validation.notBlank")
    String bicNumber;

    @Column
    @Pattern(regexp = "^DE[0-9]{9}$", message = "{vat.Pattern.message}")
    @NotBlank(message = "validation.notBlank")
    String vatId;

    @Column
    String logoUrl;
}
