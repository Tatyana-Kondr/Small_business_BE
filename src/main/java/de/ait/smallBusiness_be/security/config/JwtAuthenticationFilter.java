package de.ait.smallBusiness_be.security.config;

import de.ait.smallBusiness_be.users.dao.UsersRepository;
import de.ait.smallBusiness_be.users.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsersRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String email = null;
        String accessToken = null;

        //  Получаем Access Token из заголовка
        final String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
            try {
                email = jwtUtil.getEmailFromToken(accessToken);
            } catch (Exception e) {
                email = null; // токен недействителен или истёк
            }
        }

        //  Если Access Token невалиден, проверяем Refresh Token из cookie
        if (email == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("refreshToken".equals(cookie.getName())) {
                        String refreshToken = cookie.getValue();
                        if (jwtUtil.validateToken(refreshToken)) {
                            email = jwtUtil.getEmailFromToken(refreshToken);
                            // генерируем новый Access Token
                            accessToken = jwtUtil.generateToken(email,
                                    userRepository.findByEmail(email)
                                            .orElseThrow(() -> new RuntimeException("User not found"))
                                            .getRole().name());
                            // при желании можно отправить новый Access Token в заголовок
                            response.setHeader("Authorization", "Bearer " + accessToken);
                        }
                        break;
                    }
                }
            }
        }

        //  Если email найден и пользователь ещё не аутентифицирован
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    user.getEmail(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}
