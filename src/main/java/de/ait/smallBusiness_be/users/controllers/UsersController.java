package de.ait.smallBusiness_be.users.controllers;

import de.ait.smallBusiness_be.customers.dto.CustomerDto;
import de.ait.smallBusiness_be.payments.dto.PaymentMethodDto;
import de.ait.smallBusiness_be.users.controllers.api.UsersApi;
import de.ait.smallBusiness_be.users.dto.*;
import de.ait.smallBusiness_be.users.services.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * 1/27/2025
 * Small_business_BE
 *
 * @author Chechkina (AIT TR)
 */
@RequiredArgsConstructor
@RestController
public class UsersController implements UsersApi {

    private final UsersService usersService;

    @Override
    public List<UserDto> getAllUsers() { return usersService.getAllUsers(); }

    @Override
    public UserDto getUserById(Long id) { return usersService.getUserById(id); }

    @Override
    public ResponseEntity<UserDto> updateUserRole(Long id, UpdateUserRoleDto dto) {
        UserDto updatedUser = usersService.updateUserRole(id, dto);
        return ResponseEntity.ok(updatedUser);
    }

    @Override
    public UserDto updateUserById(Long id, UpdateUserDto updateUserDto, Principal principal) {
        return usersService.updateUserById(id, updateUserDto, principal);
    }

    @Override
    public UserDto changePassword(Long id, ChangePasswordDto dto, Principal principal) {
        return usersService.changePassword(id, dto, principal);
    }

}
