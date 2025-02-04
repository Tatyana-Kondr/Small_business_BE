package de.ait.smallBusiness_be.users.controllers;

import de.ait.smallBusiness_be.users.controllers.api.UsersApi;
import de.ait.smallBusiness_be.users.dto.NewUserDto;
import de.ait.smallBusiness_be.users.dto.UserDto;
import de.ait.smallBusiness_be.users.services.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

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
    public UserDto register(NewUserDto newUser) {
        return usersService.register(newUser);
    }

}
