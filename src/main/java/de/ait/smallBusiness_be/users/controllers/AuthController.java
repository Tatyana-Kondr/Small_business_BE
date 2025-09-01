package de.ait.smallBusiness_be.users.controllers;

import de.ait.smallBusiness_be.security.config.JwtUtil;
import de.ait.smallBusiness_be.users.dto.*;
import de.ait.smallBusiness_be.users.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tags(@Tag(name = "Authentication"))
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "User Login", description = "Authenticate user and get JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Login successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = JwtResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "Invalid credentials")
    })
    @PostMapping("/login")
    public JwtResponseDto login(@RequestBody @Valid LoginRequestDto authRequest,
                                HttpServletResponse response) {
        return authService.login(authRequest, response);
    }

    @PostMapping("/refresh")
    public JwtResponseDto refreshToken(HttpServletRequest request,
                                       HttpServletResponse response) {
        return authService.refreshToken(request, response);
    }

    @Operation(summary = "Get User Profile", description = "Retrieve info about the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Profile retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized")
    })
    @GetMapping("/me")
    public UserDto getProfile() {
        // Получаем текущую аутентификацию из контекста
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }

        String email = authentication.getName(); // email хранится в principal
        return authService.getUserProfile(email);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                         HttpServletResponse response) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String email = jwtUtil.getEmailFromToken(token);
            authService.logout(response, email);
        } else {
            authService.logout(response, null);
        }
        return ResponseEntity.ok("Logout successful");
    }
}