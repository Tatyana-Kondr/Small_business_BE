package de.ait.smallBusiness_be.users.services;

import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.users.dao.UserRepository;
import de.ait.smallBusiness_be.users.dto.*;
import de.ait.smallBusiness_be.users.model.Role;
import de.ait.smallBusiness_be.users.model.User;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 1/27/2025
 * Small_business_BE
 *
 * @author Chechkina (AIT TR)
 */

@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

    private final UserRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;


    @Override
    public UserDto getUserById(Long id) {
        User user = usersRepository.findById(id).orElseThrow(()->
                new RestApiException(ErrorDescription.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = usersRepository.findAll();
        return users.stream().map(u -> modelMapper.map(u, UserDto.class)).collect(Collectors.toList());
    }

    @Override
    public UserDto updateUserById(Long id, UpdateUserDto updateUserDto, Principal principal) {

        User currentUser = usersRepository.findByUsername(principal.getName()).orElseThrow(()->
                new RestApiException(ErrorDescription.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

        // 2. Только админ может обновлять других пользователей
        if (currentUser.getRole() != Role.ADMIN) {
            throw new RestApiException(ErrorDescription.FORBIDDEN, HttpStatus.FORBIDDEN);
        }

        User user = usersRepository.findById(id).orElseThrow(()->
                new RestApiException(ErrorDescription.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

        if (updateUserDto.getUsername() != null) {
            boolean usernameExists = usersRepository.existsByUsername(updateUserDto.getUsername());
            if (usernameExists && !user.getUsername().equals(updateUserDto.getUsername())) {
                throw new RestApiException(ErrorDescription.USERNAME_ALREADY_EXISTS, HttpStatus.CONFLICT);
            }
            user.setUsername(updateUserDto.getUsername());
        }

        if (updateUserDto.getEmail() != null) {
            user.setEmail(updateUserDto.getEmail());
        }

        usersRepository.save(user);

        return modelMapper.map(user, UserDto.class);
    }

    @Override
    @Transactional
    public UserDto updateUserRole(Long userId, UpdateUserRoleDto dto) {
        User user = usersRepository.findById(userId).orElseThrow(() ->
                new RestApiException(ErrorDescription.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

        try {
            Role newRole = Role.valueOf(dto.getRole().toUpperCase());
            user.setRole(newRole);
        } catch (IllegalArgumentException e) {
            throw new RestApiException(ErrorDescription.INVALID_ROLE, HttpStatus.BAD_REQUEST);
        }

        User updatedUser = usersRepository.save(user);
        return modelMapper.map(updatedUser, UserDto.class);
    }

    @Override
    @Transactional
    public UserDto changePassword(Long id, ChangePasswordDto dto, Principal principal) {

        User currentUser = usersRepository.findByUsername(principal.getName())
                .orElseThrow(() ->
                        new RestApiException(ErrorDescription.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

        // Найдём пользователя, чей пароль нужно менять
        User user = usersRepository.findById(id).orElseThrow(() ->
                new RestApiException(ErrorDescription.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

        // Проверка прав
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isSelf = currentUser.getId().equals(id);

        if (!isAdmin && !isSelf) {
            throw new RestApiException(ErrorDescription.FORBIDDEN, HttpStatus.FORBIDDEN);
        }

        // Если меняет сам себе — проверяем старый пароль
        if (!isAdmin) {
            if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
                throw new RestApiException(ErrorDescription.INVALID_PASSWORD, HttpStatus.BAD_REQUEST);
            }
        }

        // Устанавливаем новый пароль
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));

        usersRepository.save(user);

        return modelMapper.map(user, UserDto.class);
    }
}
