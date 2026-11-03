
package de.ait.smallBusiness_be.users.services;

import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.security.config.JwtUtil;
import de.ait.smallBusiness_be.users.dao.UserRepository;
import de.ait.smallBusiness_be.users.dto.AuthRequestDto;
import de.ait.smallBusiness_be.users.dto.AuthResponseDto;
import de.ait.smallBusiness_be.users.dto.NewUserDto;
import de.ait.smallBusiness_be.users.dto.UserDto;
import de.ait.smallBusiness_be.users.model.RefreshToken;
import de.ait.smallBusiness_be.users.model.Role;
import de.ait.smallBusiness_be.users.model.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

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

    // -------------------- REGISTER --------------------

    public UserDto register(NewUserDto newUser, Principal principal) {

        // Проверяем авторизацию
        if (principal == null) {
            throw new RestApiException(
                    ErrorDescription.UNAUTHORIZED,
                    HttpStatus.UNAUTHORIZED
            );
        }

        // Получаем текущего пользователя
        User currentUser = userRepository
                .findByUsername(principal.getName())
                .orElseThrow(() -> new RestApiException(
                        ErrorDescription.USER_NOT_FOUND,
                        HttpStatus.UNAUTHORIZED
                ));

        // Только администратор может регистрировать
        if (currentUser.getRole() != Role.ADMIN) {
            throw new RestApiException(
                    ErrorDescription.FORBIDDEN,
                    HttpStatus.FORBIDDEN
            );
        }

        // Проверяем уникальность username
        if (userRepository.existsByUsername(newUser.getUsername())) {
            throw new RestApiException(
                    ErrorDescription.USERNAME_ALREADY_EXISTS,
                    HttpStatus.CONFLICT
            );
        }

        if (userRepository.existsByEmail(newUser.getEmail())) {
            throw new RestApiException(
                    ErrorDescription.EMAIL_ALREADY_EXISTS,
                    HttpStatus.CONFLICT
            );
        }

        // Создаём пользователя
        User user = modelMapper.map(newUser, User.class);

        user.setPassword(
                passwordEncoder.encode(newUser.getPassword())
        );

        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        log.info("New user registered: {}", savedUser.getUsername());

        return modelMapper.map(savedUser, UserDto.class);
    }

    // -------------------- LOGIN --------------------
    public AuthResponseDto login(AuthRequestDto authRequest) {

        User user = userRepository
                .findByUsername(authRequest.getUsername())
                .orElseThrow(() -> new RestApiException(ErrorDescription.USER_NOT_FOUND, HttpStatus.UNAUTHORIZED));

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new RestApiException(ErrorDescription.INVALID_PASSWORD, HttpStatus.UNAUTHORIZED);
        }

        String accessToken = jwtUtil.generateAccessToken(
                user.getUsername(),
                user.getRole().name()
        );

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user.getId());

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .role(user.getRole().name())
                .build();
    }

    // -------------------- REFRESH --------------------
    @Transactional
    public AuthResponseDto refreshToken(String token) {

        if (token == null || token.isBlank()) {
            throw new RestApiException(
                    ErrorDescription.REFRESH_TOKEN_MISSING,
                    HttpStatus.UNAUTHORIZED
            );
        }

        RefreshToken refreshToken = refreshTokenService
                .findByToken(token)
                .orElseThrow(() -> new RestApiException(
                        ErrorDescription.REFRESH_TOKEN_NOT_FOUND,
                        HttpStatus.UNAUTHORIZED
                ));

        if (refreshTokenService.isExpired(refreshToken)) {

            refreshTokenService.delete(refreshToken);

            throw new RestApiException(
                    ErrorDescription.REFRESH_TOKEN_EXPIRED,
                    HttpStatus.UNAUTHORIZED
            );
        }

        User user = refreshToken.getUser();

        String newAccessToken = jwtUtil.generateAccessToken(
                user.getUsername(),
                user.getRole().name()
        );

        // Refresh token rotation
        RefreshToken rotated =
                refreshTokenService.rotate(refreshToken);

        return AuthResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(rotated.getToken())
                .role(user.getRole().name())
                .build();
    }

    // -------------------- LOGOUT --------------------
    public void logout(String refreshToken) {

        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService
                    .findByToken(refreshToken)
                    .ifPresent(refreshTokenService::delete);
        }

        SecurityContextHolder.clearContext();
    }

    // -------------------- GET USER PROFILE --------------------
    public UserDto getUserProfile(String username) {

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new RestApiException(ErrorDescription.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
