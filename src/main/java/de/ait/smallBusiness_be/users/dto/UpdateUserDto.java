package de.ait.smallBusiness_be.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(
        name = "Update User",
        description = "Data for updating a user"
)
public class UpdateUserDto {

    @NotBlank(message = "Username is required")
    @Schema(description = "Username", example = "user_1")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Email address", example = "user@gmail.com")
    private String email;
}

