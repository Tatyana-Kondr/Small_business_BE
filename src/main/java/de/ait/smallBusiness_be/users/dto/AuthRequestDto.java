package de.ait.smallBusiness_be.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO representing login request with user credentials")
public class AuthRequestDto{

        @Schema(description = "Username", example = "user1")
        String username;

        @Schema(description = "Password of the user", example = "Qwerty007!")
        String password;

}
