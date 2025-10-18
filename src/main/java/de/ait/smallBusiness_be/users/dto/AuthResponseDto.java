package de.ait.smallBusiness_be.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "JWT Response", description = "JWT token response after successful login")
public class AuthResponseDto {
    @Schema(description = "JWT token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "Refresh token", example = "gxkFKhbGcOiJIUzI1NiIsInR5cCI6IkpCJ7..")
    private String refreshToken;

    @Schema(description = "User role", example = "ADMIN")
    private String role;
}
