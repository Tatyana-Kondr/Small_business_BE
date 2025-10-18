package de.ait.smallBusiness_be.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "UserRole", description = "DTO representing authenticated or registered userRole information")
public class UpdateUserRoleDto {

    @NotBlank(message = "Role must not be blank")
    @Schema(description = "user's role", example = "ADMIN")
    private String role;
}

