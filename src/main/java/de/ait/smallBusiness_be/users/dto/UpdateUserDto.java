package de.ait.smallBusiness_be.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "New User", description = "Registration data")
public class UpdateUserDto {

    @NotNull
    @Schema(description = "User's name", example = "user_1")
    private String username;

    @Email
    @NotNull
    @Schema(description = "User's email address", example = "user@gmail.com")
    private String email;
}
