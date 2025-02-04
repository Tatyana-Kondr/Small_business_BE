package de.ait.smallBusiness_be.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO representing login request with user credentials")
public record LoginRequestDto(
        @Schema(description = "Email of the user", example = "user@gmail.com")
        String email,

        @Schema(description = "Password of the user", example = "Qwerty007!")
        String password
) {
}
