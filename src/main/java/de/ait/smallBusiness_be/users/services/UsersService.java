package de.ait.smallBusiness_be.users.services;

import de.ait.smallBusiness_be.users.dto.*;

import java.security.Principal;
import java.util.List;

public interface UsersService {

    UserDto getUserById(Long id);
    List<UserDto> getAllUsers();
    UserDto updateUserById(Long id, UpdateUserDto updateUserDto, Principal principal);
    UserDto updateUserRole(Long userId, UpdateUserRoleDto dto);
    UserDto changePassword(Long id, ChangePasswordDto dto, Principal principal);
}
