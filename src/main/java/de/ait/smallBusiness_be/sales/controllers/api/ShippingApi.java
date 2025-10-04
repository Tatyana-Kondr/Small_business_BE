package de.ait.smallBusiness_be.sales.controllers.api;

import de.ait.smallBusiness_be.exceptions.ErrorResponseDto;
import de.ait.smallBusiness_be.sales.dto.NewShippingDto;
import de.ait.smallBusiness_be.sales.dto.ShippingDto;
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
        @Tag(name = "Shipping controller")
)
@RequestMapping("/api/shippings")
public interface ShippingApi {
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @Operation(
            summary = "Add a new shipping",
            description = "Create a new shipping. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Shipping created successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ShippingDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid shipping data.",
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
    ShippingDto addShipping(
            @RequestBody @Valid NewShippingDto newShippingDto);


    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(
            summary = "Get all shippings.",
            description = "Retrieve a list of all shippings.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "List of shippings retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation =ShippingDto[].class))),
            @ApiResponse(responseCode = "404",
                    description = "No shippings found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string")))
    })
    @ResponseStatus(HttpStatus.OK)
    List<ShippingDto> getAllShippings();


    @GetMapping("/{id}")
    @Operation(
            summary = "Get shipping by ID",
            description = "Retrieve a shipping by its ID. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Shipping retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ShippingDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Shipping not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    ShippingDto getShippingById(@PathVariable Long id);


    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    @Operation(
            summary = "Update the shipping",
            description = "Update the shipping. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Shipping updated successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ShippingDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid shipping data.",
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
    ShippingDto updateShipping(
            @PathVariable Long id,
            @RequestBody @Valid NewShippingDto newShippingDto);


    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete shipping by ID",
            description = "Delete an existing shipping. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Shipping deleted successfully."),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Shipping not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteShipping(@PathVariable Long id);
}

