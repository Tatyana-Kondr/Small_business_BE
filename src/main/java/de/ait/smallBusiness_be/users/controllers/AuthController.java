package de.ait.smallBusiness_be.users.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ait.smallBusiness_be.security.details.AuthenticatedUser;
import de.ait.smallBusiness_be.users.controllers.api.AuthApi;
import de.ait.smallBusiness_be.users.dto.LoginRequestDto;
import de.ait.smallBusiness_be.users.dto.LoginResponseDto;
import de.ait.smallBusiness_be.users.dto.UserDto;
import de.ait.smallBusiness_be.users.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @Override
    public String login(@RequestBody @Valid LoginRequestDto authRequest, HttpServletRequest request) {
        authService.login(authRequest, request);

        // Возвращаем объект в формате JSON
        LoginResponseDto response = new LoginResponseDto("Login successful", HttpStatus.OK.value());

        // Преобразуем объект в JSON с использованием ObjectMapper
        try {
            return objectMapper.writeValueAsString(response);  // Убедитесь, что используется инжектированный ObjectMapper
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize response", e);
        }
    }

    @Override
    public String logout(HttpServletRequest request) {
        authService.logout(request);
        return "Logout successful";
    }

    @Override
    public UserDto getProfile(AuthenticatedUser currentUser) {
        return authService.getUserProfile(currentUser);
    }
}