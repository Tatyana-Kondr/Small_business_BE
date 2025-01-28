package de.ait.smallBusiness_be.users.services;

import de.ait.smallBusiness_be.users.dto.NewUserDto;
import de.ait.smallBusiness_be.users.dto.UserDto;

public interface UsersService {

    UserDto register(NewUserDto newUser);

}
