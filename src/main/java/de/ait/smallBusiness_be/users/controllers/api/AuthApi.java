package de.ait.smallBusiness_be.users.controllers.api;

import de.ait.smallBusiness_be.exceptions.ErrorResponseDto;
import de.ait.smallBusiness_be.security.details.AuthenticatedUser;
import de.ait.smallBusiness_be.users.dto.LoginRequestDto;
import de.ait.smallBusiness_be.users.dto.LoginResponseDto;
import de.ait.smallBusiness_be.users.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tags(@Tag(name = "Authentication"))
@RequestMapping("/api/auth")
public interface AuthApi {

    @Operation(summary = "User Login", description = "Authenticate user and start a session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Login successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    String login(@RequestBody @Valid LoginRequestDto authRequest, HttpServletRequest request);

    @Operation(summary = "User Logout", description = "Terminate the session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful")
    })
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    String logout(HttpServletRequest request);

    @Operation(summary = "Get User Profile", description = "Retrieve information about the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Profile retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "User is not authenticated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    UserDto getProfile(@Parameter(hidden = true)
                       @AuthenticationPrincipal AuthenticatedUser currentUser);
}