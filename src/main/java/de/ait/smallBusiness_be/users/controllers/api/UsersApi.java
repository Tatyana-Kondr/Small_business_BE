package de.ait.smallBusiness_be.users.controllers.api;

import de.ait.smallBusiness_be.exceptions.ErrorResponseDto;
import de.ait.smallBusiness_be.users.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Tags(
        @Tag(name = "Users")
)
@RequestMapping("/api/users")
public interface UsersApi {

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(
            summary = "Get all users",
            description = "Retrieve a list of all users. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "List of Users retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto[].class))),
            @ApiResponse(responseCode = "404",
                    description = "No users found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string")))
    })
    @ResponseStatus(HttpStatus.OK)
    List<UserDto> getAllUsers();


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    @Operation(
            summary = "Get user by ID",
            description = "Retrieve a user by its ID. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "User retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "User not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    UserDto getUserById(@PathVariable Long id);

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/role")
    @Operation(
            summary = "Update user role by ID",
            description = "Allows ADMIN to update a user's role (ADMIN or USER)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "User's role updated successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "User not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<UserDto> updateUserRole(@PathVariable Long id,
                                           @RequestBody UpdateUserRoleDto dto);
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    @Operation(
            summary = "Update user by ID",
            description = "Allows ADMIN to update a user's username and email."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "User updated successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "User not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409",
                    description = "Username already exists.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    UserDto updateUserById(@PathVariable Long id,
                           @RequestBody UpdateUserDto updateUserDto,
                           Principal principal);

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}/change-password")
    @Operation(
            summary = "Change user's password",
            description = "Allows user to change own password or ADMIN to change any user's password."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Password changed successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid old password or new password format.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden, user cannot change other user's password.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "User not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    UserDto changePassword(@PathVariable Long id,
                           @RequestBody ChangePasswordDto dto,
                           Principal principal);
}
