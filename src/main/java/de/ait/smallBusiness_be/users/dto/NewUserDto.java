package de.ait.smallBusiness_be.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "Username is required")
    @Schema(description = "Username", example = "user_1")
    private String username;

    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
            message = "Password must contain at least 8 characters, uppercase and lowercase letters, a number and a special character"
    )
    @Schema(description = "Password", example = "Qwerty007!")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Email address", example = "user@gmail.com")
    private String email;
}

