package de.ait.smallBusiness_be.payments.controllers.api;

import de.ait.smallBusiness_be.exceptions.ErrorResponseDto;
import de.ait.smallBusiness_be.payments.dto.NewPaymentProcessDto;
import de.ait.smallBusiness_be.payments.dto.PaymentProcessDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tags(
        @Tag(name = "Payment process controller")
)
@RequestMapping("/api/payment-processes")
public interface PaymentProcessApi {

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @Operation(
            summary = "Add a new payment process",
            description = "Create a new payment process. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Payment process created successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentProcessDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid payment process data.",
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
    PaymentProcessDto addPaymentProcess(
            @RequestBody @Valid NewPaymentProcessDto newPaymentProcessDto);


    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(
            summary = "Get all processes of payment.",
            description = "Retrieve a list of all processes of payment.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "List of payment's processes retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentProcessDto[].class))),
            @ApiResponse(responseCode = "404",
                    description = "No payment's processes found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string")))
    })
    @ResponseStatus(HttpStatus.OK)
    List<PaymentProcessDto> getAllPaymentProcesses();


    @GetMapping("/{id}")
    @Operation(
            summary = "Get payment process by ID",
            description = "Retrieve a payment process by its ID. Allowed to all users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Payment process retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentProcessDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Payment process not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    PaymentProcessDto getPaymentProcessById(@PathVariable Long id);


    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    @Operation(
            summary = "Update the payment process",
            description = "Update the payment process. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Payment process updated successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentProcessDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid payment process data.",
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
    PaymentProcessDto updatePaymentProcess(
            @PathVariable Long id,
            @RequestBody @Valid NewPaymentProcessDto newPaymentProcessDto);


    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete payment process by ID",
            description = "Delete an existing payment process. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Payment process deleted successfully."),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Payment process not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deletePaymentProcess(@PathVariable Long id);
}
