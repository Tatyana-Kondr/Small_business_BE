package de.ait.smallBusiness_be.users.services;

import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.security.config.JwtUtil;
import de.ait.smallBusiness_be.users.dao.UserRepository;
import de.ait.smallBusiness_be.users.dto.AuthRequestDto;
import de.ait.smallBusiness_be.users.dto.AuthResponseDto;
import de.ait.smallBusiness_be.users.dto.NewUserDto;
import de.ait.smallBusiness_be.users.model.RefreshToken;
import de.ait.smallBusiness_be.users.dto.UserDto;
import de.ait.smallBusiness_be.users.model.Role;
import de.ait.smallBusiness_be.users.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;


/**
 * 1/30/2025
 * Small_business_BE
 *
 * @author Chechkina (AIT TR)
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final ModelMapper modelMapper;

    private static final boolean IS_PROD = false; // true в продакшене

    // -------------------- REGISTER --------------------
    public UserDto register(NewUserDto newUser, Principal principal) {
        // 1. Найдём текущего пользователя (тот, кто вызывает register)
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() ->  new RestApiException(ErrorDescription.USERNAME_ALREADY_EXISTS, HttpStatus.CONFLICT));

        // 2. Проверим, что он админ
        if (currentUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("Только администратор может регистрировать новых пользователей!");
        }

        // 3. Проверим, что username ещё не занят
        if (userRepository.existsByUsername(newUser.getUsername())) {
            throw new RuntimeException("Пользователь с таким username уже существует!");
        }

        // 4. Создадим нового пользователя
        User user = modelMapper.map(newUser, User.class);
        user.setPassword(passwordEncoder.encode(newUser.getPassword()));
        user.setRole(Role.USER); // по умолчанию USER

        userRepository.save(user);

        // 6. Возвращаем UserDto
        return modelMapper.map(user, UserDto.class);
}

    // -------------------- LOGIN --------------------
    public AuthResponseDto login(AuthRequestDto authRequest, HttpServletResponse response) {

        User user = userRepository.findByUsername(authRequest.getUsername())
                .orElseThrow(() -> new RestApiException(
                        ErrorDescription.USER_NOT_FOUND,
                        HttpStatus.UNAUTHORIZED));

        // Теперь проверяем пароль
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new RestApiException(ErrorDescription.INVALID_PASSWORD, HttpStatus.UNAUTHORIZED);
        }

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getUsername(), null,  List.of(new SimpleGrantedAuthority(user.getRole().name())))
        );

        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getRole().name());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        setRefreshCookie(response, refreshToken.getToken());

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .role(user.getRole().name())
                .build();
    }



    // -------------------- REFRESH --------------------
    public AuthResponseDto refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String token = extractRefreshToken(request);

        RefreshToken refreshToken = refreshTokenService.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Refresh token not found"));

        if (refreshTokenService.isExpired(refreshToken)) {
            refreshTokenService.delete(refreshToken);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Refresh token expired");
        }

        User user = refreshToken.getUser();
        String newAccessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getRole().name());

        // Можно обновить срок действия refresh-токена в базе, если нужно
        setRefreshCookie(response, refreshToken.getToken());

        return AuthResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken.getToken())
                .role(user.getRole().name())
                .build();
    }

    // -------------------- LOGOUT --------------------
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String token = extractRefreshToken(request);
        if (token != null) {
            refreshTokenService.findByToken(token).ifPresent(refreshTokenService::delete);
        }
        clearRefreshCookie(response);
        SecurityContextHolder.clearContext();
    }

    // -------------------- GET USER PROFILE --------------------
    public UserDto getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    // -------------------- HELPER METHODS --------------------
    private void setRefreshCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(IS_PROD);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 дней
        cookie.setAttribute("SameSite", IS_PROD ? "None" : "Lax");
        response.addCookie(cookie);
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(IS_PROD);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", IS_PROD ? "None" : "Lax");
        response.addCookie(cookie);
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> "refreshToken".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}