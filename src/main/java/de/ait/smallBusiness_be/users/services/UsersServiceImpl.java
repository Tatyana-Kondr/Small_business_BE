package de.ait.smallBusiness_be.users.services;

import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.security.details.AuthenticatedUser;
import de.ait.smallBusiness_be.users.dao.UsersRepository;
import de.ait.smallBusiness_be.users.dto.NewUserDto;
import de.ait.smallBusiness_be.users.dto.UserDto;
import de.ait.smallBusiness_be.users.model.User;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * 1/27/2025
 * Small_business_BE
 *
 * @author Chechkina (AIT TR)
 */

@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public UserDto register(NewUserDto newUser) {

        checkIfExistsByEmail(newUser);

        User user = saveNewUser(newUser);

        // Аутентифицируем пользователя сразу после регистрации
        //authenticateUser(user);

        return modelMapper.map(user, UserDto.class);
    }

//    private void authenticateUser(User user) {
//        // Создаем объект Authentication
//        UsernamePasswordAuthenticationToken authenticationToken =
//                new UsernamePasswordAuthenticationToken(
//                        new AuthenticatedUser(user), // principal
//                        null, // credentials
//                        Collections.singleton(new SimpleGrantedAuthority(user.getRole().toString())) // роли
//                );
//
//        // Устанавливаем Authentication в SecurityContext
//        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
//
//        System.out.println("User authenticated: " + user.getEmail());
//    }

    private void checkIfExistsByEmail(NewUserDto newUser) {
        if (usersRepository.existsByEmail(newUser.getEmail())) {
            throw new RestApiException(ErrorDescription.USER_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }
    }

    private User saveNewUser(NewUserDto newUser) {
        User user = User.builder()
                .email(newUser.getEmail())
                .password(passwordEncoder.encode(newUser.getPassword()))
                .role(User.Role.USER)
                .state(User.State.CONFIRMED)
                .build();

        System.out.println("Before saving user: " + user);
        User savedUser = usersRepository.save(user);
        System.out.println("After saving user: " + savedUser);

        return savedUser;
    }

    public UserDto getUserById(Long currentUserId) {
        User user = usersRepository.findById(currentUserId).orElseThrow(()->
                new RestApiException(ErrorDescription.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
        return modelMapper.map(user, UserDto.class);
    }



}
