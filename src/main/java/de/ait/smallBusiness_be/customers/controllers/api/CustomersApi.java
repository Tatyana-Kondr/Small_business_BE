package de.ait.smallBusiness_be.customers.controllers.api;

import de.ait.smallBusiness_be.customers.dto.CustomerDto;
import de.ait.smallBusiness_be.customers.dto.NewCustomerDto;
import de.ait.smallBusiness_be.exceptions.ErrorResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * @author admin
 * @date 28.11.2024
 */

@Tags(
        @Tag(name = "Customers")
)
@RequestMapping("/api/customers")
public interface CustomersApi {

    //@PreAuthorize("isAuthenticated()")
    @PostMapping
    @Operation(
            summary = "Add a new customer",
            description = "Create a new customer. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Customer created successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomerDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid customer data.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409",
                    description = "Customer with the same name and address already exists.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string")))
    })
    @ResponseStatus(HttpStatus.CREATED)
    CustomerDto createCustomer(
            @RequestBody @Valid NewCustomerDto newCustomerDto);

    //@PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(
            summary = "Get all customers",
            description = "Retrieve a list of all customers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "List of Customers retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomerDto[].class))),
            @ApiResponse(responseCode = "404",
                    description = "No customers found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string")))
    })
    @ResponseStatus(HttpStatus.OK)
    Page<CustomerDto> getAllCustomers(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(defaultValue = "name") String sort);

    //@PreAuthorize("isAuthenticated()")
    @GetMapping("/customer-number")
    @Operation(
            summary = "Get all customers with customer number",
            description = "Retrieve a list of all customers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "List of Customers retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomerDto[].class))),
            @ApiResponse(responseCode = "404",
                    description = "No customers found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string")))
    })
    @ResponseStatus(HttpStatus.OK)
    Page<CustomerDto> getAllCustomersWithCustomerNumber(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(defaultValue = "name") String sort);

    @GetMapping("/{id}")
    @Operation(
            summary = "Get customer by ID",
            description = "Retrieve a customer by its ID. Allowed to all users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Customer retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomerDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Customer not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    CustomerDto getCustomerById(@PathVariable Long id);

    //@PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    @Operation(
            summary = "Update the customer",
            description = "Update the customer. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Customer updated successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomerDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid customer data.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409",
                    description = "Customer with the same name and address already exists.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string")))
    })
    @ResponseStatus(HttpStatus.OK)
    CustomerDto updateCustomer(
            @PathVariable Long id,
            @RequestBody @Valid NewCustomerDto newCustomerDto);

    //@PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete customer by ID",
            description = "Delete an existing customer. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Customer deleted successfully."),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Customer not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteCustomer(
            @PathVariable Long id);

}
