package de.ait.smallBusiness_be.users.controllers;

import de.ait.smallBusiness_be.exceptions.ErrorResponseDto;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.users.dto.AuthRequestDto;
import de.ait.smallBusiness_be.users.dto.AuthResponseDto;
import de.ait.smallBusiness_be.users.dto.NewUserDto;
import de.ait.smallBusiness_be.users.dto.UserDto;
import de.ait.smallBusiness_be.users.services.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.security.Principal;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tags(@Tag(name = "Authentication"))
public class AuthController {

    private final AuthService authService;

    // -------------------- REGISTER --------------------
    @Operation(summary = "Register new user (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden, only admin can register"),
            @ApiResponse(responseCode = "409", description = "Conflict, username already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody @Valid NewUserDto newUser, Principal principal) {
        log.info("User '{}' is trying to register new user '{}'", principal.getName(), newUser.getUsername());
        UserDto createdUser = authService.register(newUser, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    // -------------------- LOGIN --------------------
    @Operation(summary = "User Login", description = "Authenticate user and get JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Login successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthRequestDto authRequest,
                                   HttpServletResponse response) {
        try {
            AuthResponseDto authResponse = authService.login(authRequest, response);
            return ResponseEntity.ok(authResponse);
        } catch (RestApiException e) {
            // Кастомные ошибки с понятным message
            return ResponseEntity.status(e.getStatus())
                    .body(new ErrorResponseDto(e.getMessage()));
        } catch (Exception e) {
            // Неожиданные ошибки
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("Internal server error"));
        }
    }


    // -------------------- REFRESH --------------------
    @Operation(summary = "Refresh JWT token using refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "403", description = "Refresh token invalid or expired")
    })
    @PostMapping("/refresh")
    public AuthResponseDto refreshToken(HttpServletRequest request, HttpServletResponse response) {
        log.info("Refresh token requested");
        return authService.refreshToken(request, response);
    }

    // -------------------- LOGOUT --------------------
    @Operation(summary = "Logout user and invalidate refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful")
    })
    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("Logout requested");
        authService.logout(request, response);
    }

    // -------------------- GET USER PROFILE --------------------
    @Operation(summary = "Get current user profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/me")
    public UserDto getUserProfile(Principal principal) {
        log.info("Fetching profile for user '{}'", principal.getName());
        return authService.getUserProfile(principal.getName());
    }
}
