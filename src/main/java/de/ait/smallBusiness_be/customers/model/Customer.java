package de.ait.smallBusiness_be.customers.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static de.ait.smallBusiness_be.constaints.EntityValidationConstants.*;

/**
 * @author Kondratyeva
 * @date 28.11.2024
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @NotBlank(message = "validation.notBlank")
    @Size(min = 3, max = 100, message = "{validation.name.size}")
    private String name;

    @Column(unique = true)
    @Size( max = 6, message = "{validation.name.size}")
    private String customerNumber;

    @Embedded
    private Address address;

    @Column
    @Pattern(regexp = PHONE_REGEX, message = "{phone.Pattern.message}")
    private String phone;

    @Column
    @Pattern(regexp = EMAIL_REGEX, message = "{email.Pattern.message}")
    private String email;

    @Column
    @Pattern(regexp = WEBSITE_REGEX, message = "{website.Pattern.message}")
    private String website;

}
