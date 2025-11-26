package de.ait.smallBusiness_be.sales.controllers.api;

import de.ait.smallBusiness_be.exceptions.ErrorResponseDto;
import de.ait.smallBusiness_be.sales.dto.NewTermOfPaymentDto;
import de.ait.smallBusiness_be.sales.dto.TermOfPaymentDto;
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
        @Tag(name = "Term Of Payment controller")
)
@RequestMapping("/api/payment-terms")
public interface TermOfPaymentApi {

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @Operation(
            summary = "Add a new term of payment",
            description = "Create a new term of payment.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Term of payment created successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TermOfPaymentDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid term of payment data.",
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
    TermOfPaymentDto addTermOfPayment(
            @RequestBody @Valid NewTermOfPaymentDto newTerm);


    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(
            summary = "Get all terms of payment.",
            description = "Retrieve a list of all terms of payment.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "List of terms of payment retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TermOfPaymentDto[].class))),
            @ApiResponse(responseCode = "404",
                    description = "No terms of payment found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string")))
    })
    @ResponseStatus(HttpStatus.OK)
    List<TermOfPaymentDto> getAllTermsOfPayment();

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    @Operation(
            summary = "Get term of payment by ID",
            description = "Retrieve a term of payment by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Term of payment retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TermOfPaymentDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "term of payment not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    TermOfPaymentDto getTermOfPaymentById(@PathVariable Long id);


    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    @Operation(
            summary = "Update the term of payment",
            description = "Update the term of payment.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Term of payment updated successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TermOfPaymentDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid term of payment data.",
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
    TermOfPaymentDto updateTermOfPayment(
            @PathVariable Long id,
            @RequestBody @Valid NewTermOfPaymentDto newTerm);


    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete term of payment by ID",
            description = "Delete an existing term of payment.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Term of payment deleted successfully."),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Term of payment not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteTermOfPayment(@PathVariable Long id);
}