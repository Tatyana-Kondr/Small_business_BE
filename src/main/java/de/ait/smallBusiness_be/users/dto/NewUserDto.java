package de.ait.smallBusiness_be.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 1/27/2025
 * Small_business_BE
 *
 * @author Chechkina (AIT TR)
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "New User", description = "Registration data")
public class NewUserDto {

    @NotNull
    @Schema(description = "User's name", example = "user_1")
    private String username;

    @NotNull
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)\\S{4,}$")
    @Schema(description = "User's password", example = "Qwerty007!")
    private String password;

    @Email
    @NotNull
    @Schema(description = "User's email address", example = "user@gmail.com")
    private String email;
}
