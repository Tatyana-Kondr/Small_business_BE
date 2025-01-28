package de.ait.smallBusiness_be.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "User", description = "DTO representing authenticated or registered user information")
public class UserDto {

    @Schema(description = "user ID", example = "1")
    private Long id;

    @Schema(description = "User's email address", example = "user@gmail.com")
    private String email;

    @Schema(description = "Role assigned to the user", example = "USER")
    private String role;
}
