package de.ait.smallBusiness_be.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO representing login request with user credentials")
public record LoginRequestDto(
        @Schema(description = "Email of the user", example = "admin@gmail.de")
        String email,

        @Schema(description = "Password of the user", example = "Password007!")
        String password
) {
}
