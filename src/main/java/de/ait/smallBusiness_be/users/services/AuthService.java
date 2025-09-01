package de.ait.smallBusiness_be.users.services;

import de.ait.smallBusiness_be.security.config.JwtUtil;
import de.ait.smallBusiness_be.users.dao.UsersRepository;
import de.ait.smallBusiness_be.users.dto.JwtResponseDto;
import de.ait.smallBusiness_be.users.dto.LoginRequestDto;
import de.ait.smallBusiness_be.users.dto.UserDto;
import de.ait.smallBusiness_be.users.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 1/30/2025
 * Small_business_BE
 *
 * @author Chechkina (AIT TR)
 */

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsersRepository userRepository;
    private final ModelMapper mapper;
    private final JwtUtil jwtUtil;

    // Логин
    public JwtResponseDto login(LoginRequestDto authRequest, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.email(), authRequest.password())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Генерация токенов
        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        // Устанавливаем HttpOnly cookie для refresh токена
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(30 * 24 * 60 * 60); // 30 дней
        response.addCookie(cookie);

        return JwtResponseDto.builder()
                .token(accessToken)
                .type("Bearer")
                .build();
    }

    // Refresh
    public JwtResponseDto refreshToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) throw new RuntimeException("No cookies found");

        String refreshToken = null;
        for (Cookie c : cookies) {
            if ("refreshToken".equals(c.getName())) refreshToken = c.getValue();
        }

        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String email = jwtUtil.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new RuntimeException("Refresh token mismatch");
        }

        // Генерация новых токенов
        String newAccessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        Cookie cookie = new Cookie("refreshToken", newRefreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(30 * 24 * 60 * 60); // 30 дней
        response.addCookie(cookie);

        return JwtResponseDto.builder()
                .token(newAccessToken)
                .type("Bearer")
                .build();
    }

    // Logout
    public void logout(HttpServletResponse response, String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            user.setRefreshToken(null);  // удаляем refresh из базы
            userRepository.save(user);
        }
        // Удаляем refreshToken cookie
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // обязательно, если приложение работает по HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(0); // мгновенное удаление
        response.addCookie(cookie);
        // Дополнительно выставляем SameSite
        response.addHeader("Set-Cookie",
                "refreshToken=; Max-Age=0; Path=/; HttpOnly; Secure; SameSite=Strict");
        // чистим контекст
        SecurityContextHolder.clearContext();
    }

    // Получение профиля пользователя
    public UserDto getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
