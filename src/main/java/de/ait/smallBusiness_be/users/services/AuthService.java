package de.ait.smallBusiness_be.users.services;

import de.ait.smallBusiness_be.users.dao.UsersRepository;
import de.ait.smallBusiness_be.users.dto.LoginRequestDto;
import de.ait.smallBusiness_be.users.dto.UserDto;
import de.ait.smallBusiness_be.users.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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

    public void login(LoginRequestDto authRequest, HttpServletRequest request) {
        // Аутентификация пользователя
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.email(), authRequest.password())
        );

        // Устанавливаем аутентификацию в SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Создаем или получаем сессию
        HttpSession session = request.getSession();
        // Сохраняем пользователя в сессию
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        session.setAttribute("user", userDetails);
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

    public UserDto getUserProfile(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return mapper.map(user, UserDto.class);
    }
}
