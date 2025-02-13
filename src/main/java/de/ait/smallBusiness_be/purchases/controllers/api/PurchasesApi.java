package de.ait.smallBusiness_be.purchases.controllers.api;


import de.ait.smallBusiness_be.customers.dto.CustomerDto;
import de.ait.smallBusiness_be.exceptions.ErrorResponseDto;
import de.ait.smallBusiness_be.purchases.dto.NewPurchaseDto;
import de.ait.smallBusiness_be.purchases.dto.PurchaseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tags(
        @Tag(name = "Purchase controller")
)
@RequestMapping("/api/purchases")
public interface PurchasesApi {
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @Operation(
            summary = "Add a new purchase",
            description = "Create a new purchase. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Purchase created successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PurchaseDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid purchase data.",
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
    PurchaseDto createPurchase(
            @RequestBody @Valid NewPurchaseDto newPurchaseDto);

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(
            summary = "Get all purchases",
            description = "Retrieve a list of all purchases.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "List of Purchases retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PurchaseDto[].class))),
            @ApiResponse(responseCode = "404",
                    description = "No purchases found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string")))
    })
    @ResponseStatus(HttpStatus.OK)
    Page<PurchaseDto> getAllPurchases(
            @PageableDefault(size = 10, sort = "purchasingDate", direction = Sort.Direction.DESC) Pageable pageable);

    @GetMapping("/{id}")
    @Operation(
            summary = "Get purchase by ID",
            description = "Retrieve a purchase by its ID. Allowed to all users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Purchase retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PurchaseDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "purchase not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    PurchaseDto getPurchaseById(@PathVariable Long id);

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/search/{query}")
    @Operation(
            summary = "Get all purchases according to search parameters",
            description = "Retrieve a list of all purchases.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "List of Purchases retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PurchaseDto[].class))),
            @ApiResponse(responseCode = "404",
                    description = "No purchases found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string")))
    })
    @ResponseStatus(HttpStatus.OK)
    Page<PurchaseDto> searchPurchases(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(defaultValue = "purchasingDate") String sort,
            @PathVariable String query);

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/filter")
    @Operation(
            summary = "Get all purchases according to the filters",
            description = "Retrieve a list of all purchases.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "List of Purchases retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomerDto[].class))),
            @ApiResponse(responseCode = "404",
                    description = "No purchases found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string")))
    })
    @ResponseStatus(HttpStatus.OK)
    Page<PurchaseDto> getAllPurchasesByFilter(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(defaultValue = "purchasingDate") String sort,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) String document,
            @RequestParam(required = false) String documentNumber,
            @RequestParam(required = false) BigDecimal total,
            @RequestParam(required = false) String paymentStatus);

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    @Operation(
            summary = "Update the purchase",
            description = "Update the purchase. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Purchase updated successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PurchaseDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid purchase data.",
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
    PurchaseDto updatePurchase(
            @PathVariable Long id,
            @RequestBody @Valid NewPurchaseDto newPurchaseDto);

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete purchase by ID",
            description = "Delete an existing purchase. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Purchase deleted successfully."),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Purchase not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deletePurchase(
            @PathVariable Long id);

}
