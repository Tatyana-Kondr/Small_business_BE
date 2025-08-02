package de.ait.smallBusiness_be.users.services;

import de.ait.smallBusiness_be.users.dao.UsersRepository;
import de.ait.smallBusiness_be.users.dto.LoginRequestDto;
import de.ait.smallBusiness_be.users.dto.SessionUserDto;
import de.ait.smallBusiness_be.users.dto.UserDto;
import de.ait.smallBusiness_be.users.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
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

    public void login(LoginRequestDto authRequest, HttpServletRequest request) {
        // 1. Аутентификация пользователя
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.email(),
                        authRequest.password()
                )
        );

        // 2. Устанавливаем аутентификацию в SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Получаем пользователя из базы по email
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 4. Создаём DTO для хранения в сессии
        SessionUserDto sessionUser = new SessionUserDto(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        // 5. Сохраняем в сессию
        HttpSession session = request.getSession();
        session.setAttribute("user", sessionUser); // теперь это простой сериализуемый объект

        // 6. Также сохраняем SecurityContext (нужно для Spring Security)
        SecurityContext context = SecurityContextHolder.getContext();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
    }

    public void logout(HttpServletRequest request) {
        // Очищаем SecurityContext
        SecurityContextHolder.clearContext();

        // Закрываем сессию
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();  // Инвалидируем сессию
        }
    }

    public UserDto getUserProfile(SessionUserDto sessionUser) {
        User user = userRepository.findById(sessionUser.getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

}
