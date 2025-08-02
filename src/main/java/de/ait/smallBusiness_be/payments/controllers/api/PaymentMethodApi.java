package de.ait.smallBusiness_be.payments.controllers.api;

import de.ait.smallBusiness_be.exceptions.ErrorResponseDto;
import de.ait.smallBusiness_be.payments.dto.NewPaymentMethodDto;
import de.ait.smallBusiness_be.payments.dto.PaymentMethodDto;
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
        @Tag(name = "Payment method controller")
)
@RequestMapping("/api/payment-methods")
public interface PaymentMethodApi {

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @Operation(
            summary = "Add a new payment method",
            description = "Create a new payment method. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Payment method created successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentMethodDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid payment method data.",
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
    PaymentMethodDto addPaymentMethod(
            @RequestBody @Valid NewPaymentMethodDto newPaymentMethodDto);


    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(
            summary = "Get all methods of payment.",
            description = "Retrieve a list of all methods of payment.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "List of payment's methods retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentMethodDto[].class))),
            @ApiResponse(responseCode = "404",
                    description = "No payment's methods found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string")))
    })
    @ResponseStatus(HttpStatus.OK)
    List<PaymentMethodDto> getAllPaymentMethods();


    @GetMapping("/{id}")
    @Operation(
            summary = "Get payment method by ID",
            description = "Retrieve a payment method by its ID. Allowed to all users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Payment method retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentMethodDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Payment method not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    PaymentMethodDto getPaymentMethodById(@PathVariable Long id);


    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    @Operation(
            summary = "Update the payment method",
            description = "Update the payment method. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Payment method updated successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentMethodDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid payment method data.",
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
    PaymentMethodDto updatePaymentMethod(
            @PathVariable Long id,
            @RequestBody @Valid NewPaymentMethodDto newPaymentMethodDto);


    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete payment method by ID",
            description = "Delete an existing payment method. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Payment method deleted successfully."),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Payment method not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deletePaymentMethod(@PathVariable Long id);
}
