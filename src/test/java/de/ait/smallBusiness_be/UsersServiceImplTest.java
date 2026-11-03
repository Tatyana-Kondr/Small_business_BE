package de.ait.smallBusiness_be;

import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.users.dao.UserRepository;
import de.ait.smallBusiness_be.users.dto.ChangePasswordDto;
import de.ait.smallBusiness_be.users.dto.UpdateUserDto;
import de.ait.smallBusiness_be.users.dto.UpdateUserRoleDto;
import de.ait.smallBusiness_be.users.dto.UserDto;
import de.ait.smallBusiness_be.users.model.Role;
import de.ait.smallBusiness_be.users.model.User;
import de.ait.smallBusiness_be.users.services.UsersServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsersServiceImplTest {

    @Mock
    private UserRepository usersRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UsersServiceImpl usersService;

    private User user;
    private UserDto userDto;
    private Principal principal;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setUsername("testuser");

        principal = () -> "admin"; // возвращает имя пользователя
    }

    @Test
    void getUserById_success() {
        when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserDto.class)).thenReturn(userDto);

        UserDto result = usersService.getUserById(1L);

        assertEquals("testuser", result.getUsername());
    }

    @Test
    void getUserById_notFound_throws() {
        when(usersRepository.findById(1L)).thenReturn(Optional.empty());

        RestApiException ex = assertThrows(RestApiException.class,
                () -> usersService.getUserById(1L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertTrue(ex.getMessage().contains("User not found"));
    }

    @Test
    void getAllUsers_success() {
        List<User> users = List.of(user);
        when(usersRepository.findAll()).thenReturn(users);
        when(modelMapper.map(user, UserDto.class)).thenReturn(userDto);

        List<UserDto> result = usersService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
    }

    @Test
    void updateUserById_asAdmin_success() {
        User admin = new User();
        admin.setId(2L);
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);

        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setUsername("newUser");
        updateDto.setEmail("new@email.com");

        when(usersRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
        when(usersRepository.existsByUsername("newUser")).thenReturn(false);
        when(modelMapper.map(any(User.class), eq(UserDto.class))).thenReturn(userDto);

        UserDto result = usersService.updateUserById(1L, updateDto, principal);

        assertEquals(userDto, result);
        verify(usersRepository).save(user);
        assertEquals("newUser", user.getUsername());
        assertEquals("new@email.com", user.getEmail());
    }

    @Test
    void updateUserById_nonAdmin_throws() {
        Principal userPrincipal = () -> "testuser"; // обычный пользователь
        user.setRole(Role.USER);
        when(usersRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setUsername("newUser");

        RestApiException ex = assertThrows(RestApiException.class,
                () -> usersService.updateUserById(1L, updateDto, userPrincipal));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void changePassword_self_success() {
        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setOldPassword("old");
        dto.setNewPassword("new");

        user.setRole(Role.USER);
        principal = () -> "testuser";

        when(usersRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("encodedNew");
        when(modelMapper.map(user, UserDto.class)).thenReturn(userDto);

        UserDto result = usersService.changePassword(1L, dto, principal);

        assertEquals(userDto, result);
        assertEquals("encodedNew", user.getPassword());
    }

    @Test
    void changePassword_wrongOldPassword_throws() {
        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setOldPassword("wrong");
        dto.setNewPassword("new");

        user.setRole(Role.USER);
        principal = () -> "testuser";

        when(usersRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", user.getPassword())).thenReturn(false);

        RestApiException ex = assertThrows(RestApiException.class,
                () -> usersService.changePassword(1L, dto, principal));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals(ErrorDescription.INVALID_OLD_PASSWORD.getDescription(), ex.getMessage());
    }

    @Test
    void updateUserRole_invalidRole_throws() {
        UpdateUserRoleDto dto = new UpdateUserRoleDto();
        dto.setRole("Invalid user role");

        when(usersRepository.findById(1L)).thenReturn(Optional.of(user));

        RestApiException ex = assertThrows(RestApiException.class,
                () -> usersService.updateUserRole(1L, dto));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("Invalid user role"));
    }


    @Test
    void getAllUsers_empty_returnsEmptyList() {
        when(usersRepository.findAll()).thenReturn(List.of());

        List<UserDto> result = usersService.getAllUsers();

        assertTrue(result.isEmpty());
        verifyNoInteractions(modelMapper);
    }

    @Test
    void updateUserById_usernameAlreadyExists_throws() {
        User admin = new User();
        admin.setId(2L);
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);

        UpdateUserDto dto = new UpdateUserDto();
        dto.setUsername("existingUser");

        when(usersRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
        when(usersRepository.existsByUsername("existingUser")).thenReturn(true);

        RestApiException ex = assertThrows(
                RestApiException.class,
                () -> usersService.updateUserById(1L, dto, principal)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("testuser", user.getUsername());
        verify(usersRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserById_emailAlreadyExists_throws() {
        User admin = new User();
        admin.setId(2L);
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);

        user.setEmail("old@email.com");

        UpdateUserDto dto = new UpdateUserDto();
        dto.setEmail("existing@email.com");

        when(usersRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
        when(usersRepository.existsByEmail("existing@email.com")).thenReturn(true);

        RestApiException ex = assertThrows(RestApiException.class,
                () -> usersService.updateUserById(1L, dto, principal)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("old@email.com", user.getEmail());
        verify(usersRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserById_emailWasNull_success() {
        User admin = new User();
        admin.setId(2L);
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);

        user.setEmail(null);

        UpdateUserDto dto = new UpdateUserDto();
        dto.setEmail("new@email.com");

        when(usersRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserDto.class)).thenReturn(userDto);

        UserDto result = usersService.updateUserById(1L, dto, principal);

        assertNotNull(result);
        assertEquals("new@email.com", user.getEmail());
        verify(usersRepository).save(user);
    }

    @Test
    void updateUserRole_success() {
        UpdateUserRoleDto dto = new UpdateUserRoleDto();
        dto.setRole("ADMIN");

        when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
        when(usersRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, UserDto.class)).thenReturn(userDto);

        UserDto result = usersService.updateUserRole(1L, dto);

        assertNotNull(result);
        assertEquals(Role.ADMIN, user.getRole());
        verify(usersRepository).save(user);
    }

    @Test
    void updateUserRole_blankRole_throws() {
        UpdateUserRoleDto dto = new UpdateUserRoleDto();
        dto.setRole(" ");

        when(usersRepository.findById(1L)).thenReturn(Optional.of(user));

        RestApiException ex = assertThrows(RestApiException.class,
                () -> usersService.updateUserRole(1L, dto));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(usersRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserRole_userNotFound_throws() {
        UpdateUserRoleDto dto = new UpdateUserRoleDto();
        dto.setRole("ADMIN");

        when(usersRepository.findById(1L)).thenReturn(Optional.empty());

        RestApiException ex = assertThrows(RestApiException.class,
                () -> usersService.updateUserRole(1L, dto));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void changePassword_adminChangesOtherUser_success() {
        User admin = new User();
        admin.setId(2L);
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);

        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setNewPassword("newPassword");

        when(usersRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(modelMapper.map(user, UserDto.class)).thenReturn(userDto);

        UserDto result = usersService.changePassword(1L, dto, principal);

        assertNotNull(result);
        assertEquals("encodedNewPassword", user.getPassword());

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(usersRepository).save(user);
    }

    @Test
    void changePassword_nonAdminChangesOtherUser_forbidden() {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("other");
        otherUser.setPassword("otherEncodedPassword");

        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setNewPassword("newPassword");

        Principal userPrincipal = () -> "testuser";

        when(usersRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(usersRepository.findById(2L)).thenReturn(Optional.of(otherUser));

        RestApiException ex = assertThrows(RestApiException.class,
                () -> usersService.changePassword(2L, dto, userPrincipal));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());

        verify(passwordEncoder, never()).encode(anyString());
        verify(usersRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_userNotFound_throws() {
        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setNewPassword("newPassword");

        when(usersRepository.findByUsername("admin")).thenReturn(Optional.empty());

        RestApiException ex = assertThrows(RestApiException.class,
                () -> usersService.changePassword(1L, dto, principal));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        verify(usersRepository, never()).save(any(User.class));
    }

}