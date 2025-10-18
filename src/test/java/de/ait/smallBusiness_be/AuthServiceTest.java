package de.ait.smallBusiness_be;

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
import de.ait.smallBusiness_be.users.services.AuthService;
import de.ait.smallBusiness_be.users.services.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private AuthService authService;

    private User user;
    private Principal adminPrincipal;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setRole(Role.ADMIN);

        adminPrincipal = () -> "admin";
    }

    @Test
    void register_success() {
        NewUserDto newUser = new NewUserDto();
        newUser.setUsername("newuser");
        newUser.setPassword("pass");

        User admin = new User();
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.existsByUsername("newuser")).thenReturn(false);

        User userEntity = new User();
        when(modelMapper.map(newUser, User.class)).thenReturn(userEntity);
        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");
        UserDto userDto = new UserDto();
        when(modelMapper.map(userEntity, UserDto.class)).thenReturn(userDto);

        UserDto result = authService.register(newUser, adminPrincipal);

        assertEquals(userDto, result);
        assertEquals(Role.USER, userEntity.getRole());
        verify(userRepository).save(userEntity);
    }

    @Test
    void login_success() {
        AuthRequestDto request = new AuthRequestDto();
        request.setUsername("testuser");
        request.setPassword("pass");

        HttpServletResponse response = mock(HttpServletResponse.class);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any()))
                .thenReturn(mock(Authentication.class));
        when(jwtUtil.generateAccessToken("testuser", "ADMIN")).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(1L)).thenReturn(
                RefreshToken.builder().token("refresh-token").user(user).build()
        );

        AuthResponseDto result = authService.login(request, response);

        assertEquals("access-token", result.getAccessToken());
        assertEquals("refresh-token", result.getRefreshToken());
        assertEquals("ADMIN", result.getRole());
    }

    @Test
    void refreshToken_success() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        RefreshToken refreshToken = RefreshToken.builder()
                .token("refresh-token")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();

        Cookie cookie = new Cookie("refreshToken", "refresh-token");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(refreshTokenService.findByToken("refresh-token")).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.isExpired(refreshToken)).thenReturn(false);
        when(jwtUtil.generateAccessToken(user.getUsername(), user.getRole().name())).thenReturn("new-access-token");

        AuthResponseDto result = authService.refreshToken(request, response);

        assertEquals("new-access-token", result.getAccessToken());
        assertEquals("refresh-token", result.getRefreshToken());
        assertEquals("ADMIN", result.getRole());
    }

    @Test
    void logout_success() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        RefreshToken refreshToken = RefreshToken.builder()
                .token("refresh-token")
                .user(user)
                .build();

        Cookie cookie = new Cookie("refreshToken", "refresh-token");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(refreshTokenService.findByToken("refresh-token")).thenReturn(Optional.of(refreshToken));

        authService.logout(request, response);

        verify(refreshTokenService).delete(refreshToken);
    }

    @Test
    void getUserProfile_success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserDto result = authService.getUserProfile("testuser");

        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getRole(), result.getRole());
    }

    @Test
    void register_notAdmin_throws() {
        NewUserDto newUser = new NewUserDto();
        newUser.setUsername("newuser");
        newUser.setPassword("pass");

        User normalUser = new User();
        normalUser.setUsername("user");
        normalUser.setRole(Role.USER);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(normalUser));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.register(newUser, adminPrincipal));

        assertEquals("Только администратор может регистрировать новых пользователей!", ex.getMessage());
    }

    @Test
    void register_usernameExists_throws() {
        NewUserDto newUser = new NewUserDto();
        newUser.setUsername("existingUser");
        newUser.setPassword("pass");

        User admin = new User();
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.existsByUsername("existingUser")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.register(newUser, adminPrincipal));

        assertEquals("Пользователь с таким username уже существует!", ex.getMessage());
    }

    @Test
    void login_userNotFound_throws() {
        AuthRequestDto request = new AuthRequestDto();
        request.setUsername("unknown");
        request.setPassword("pass");

        HttpServletResponse response = mock(HttpServletResponse.class);

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        RestApiException ex = assertThrows(RestApiException.class,
                () -> authService.login(request, response));

        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void login_invalidPassword_throws() {
        AuthRequestDto request = new AuthRequestDto();
        request.setUsername("testuser");
        request.setPassword("wrongpass");

        HttpServletResponse response = mock(HttpServletResponse.class);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        RestApiException ex = assertThrows(RestApiException.class,
                () -> authService.login(request, response));

        assertEquals("Invalid password", ex.getMessage());
    }

    @Test
    void refreshToken_notFound_throws() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        Cookie cookie = new Cookie("refreshToken", "invalid-token");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(refreshTokenService.findByToken("invalid-token")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.refreshToken(request, response));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertEquals("Refresh token not found", ex.getReason());
    }

    @Test
    void refreshToken_expired_throws() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        RefreshToken refreshToken = RefreshToken.builder()
                .token("refresh-token")
                .user(user)
                .expiryDate(Instant.now().minusSeconds(10))
                .build();

        Cookie cookie = new Cookie("refreshToken", "refresh-token");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(refreshTokenService.findByToken("refresh-token")).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.isExpired(refreshToken)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.refreshToken(request, response));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertEquals("Refresh token expired", ex.getReason());
    }

    @Test
    void logout_noToken_shouldNotFail() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getCookies()).thenReturn(null); // нет cookie

        assertDoesNotThrow(() -> authService.logout(request, response));
    }

    @Test
    void getUserProfile_userNotFound_throws() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> authService.getUserProfile("unknown"));
    }

}

