package de.ait.smallBusiness_be.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ait.smallBusiness_be.users.dto.StandardResponseDto;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

/**
 * 1/27/2025
 * Small_business_BE
 *
 * @author Chechkina (AIT TR)
 */
public class SecurityExceptionHandlers {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static final AuthenticationEntryPoint ENTRY_POINT = (request,
                                                                response,
                                                                authException) ->
            fillResponse(response, HttpStatus.UNAUTHORIZED, "User unauthorized");

    public static final AuthenticationSuccessHandler LOGIN_SUCCESS_HANDLER = (request,
                                                                              response,
                                                                              authentication) ->
            fillResponse(response, HttpStatus.OK, "Login successful");

    public static final AuthenticationFailureHandler LOGIN_FAILURE_HANDLER =  (request,
                                                                               response,
                                                                               exception) ->
            fillResponse(response, HttpStatus.UNAUTHORIZED, "Incorrect password or username");

//    public static final AccessDeniedHandler ACCESS_DENIED_HANDLER =  (request,
//                                                                      response,
//                                                                      accessDeniedException) -> {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        fillResponse(response, HttpStatus.FORBIDDEN, "Access denied for user with email <"
//                + authentication.getName() + "> and role " + authentication.getAuthorities());
//
//    };

    public static final AccessDeniedHandler ACCESS_DENIED_HANDLER = (request, response, accessDeniedException) -> {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Проверка, что аутентификация равна null и не происходит при регистрации (например, можно по URL).
        if (authentication == null) {
            // Можно добавить проверку, например, на URL регистрации, чтобы избежать ошибок при регистрации
            if (request.getRequestURI().contains("/api/users/register")) {
                // Тут мы не делаем никаких действий при регистрации
                return;
            }

            fillResponse(response, HttpStatus.FORBIDDEN, "Access denied for unauthenticated user.");
        } else {
            fillResponse(response, HttpStatus.FORBIDDEN, "Access denied for user with email <"
                    + authentication.getName() + "> and role " + authentication.getAuthorities());
        }
    };

    public static final LogoutSuccessHandler LOGOUT_SUCCESS_HANDLER = (request,
                                                                       response,
                                                                       authentication) ->
            fillResponse(response, HttpStatus.OK, "Logout successful");

    private static void fillResponse(HttpServletResponse response, HttpStatus status, String message) {
        try {
            response.setStatus(status.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            StandardResponseDto responseDto = StandardResponseDto.builder()
                    .message(message)
                    .build();

            String body = objectMapper.writeValueAsString(responseDto);

            response.getWriter().write(body);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
