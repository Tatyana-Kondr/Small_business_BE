package de.ait.smallBusiness_be.sales.controllers.api;

import de.ait.smallBusiness_be.exceptions.ErrorResponseDto;
import de.ait.smallBusiness_be.sales.dto.NewTaxRateDto;
import de.ait.smallBusiness_be.sales.dto.TaxRateDto;
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
        @Tag(name = "Tax rate controller")
)
@RequestMapping("/api/tax-rates")
public interface TaxRateApi {

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(
            summary = "Add a new tax rate",
            description = "Create a new tax rate. Admin is allowed."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Tax rate created successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = TaxRateDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid tax rate data.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string")
                    )
            ),

            @ApiResponse(
                    responseCode = "409",
                    description = "Tax rate conflict or operation forbidden by business rules.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponseDto.class
                            )
                    )
            )

    })
    @ResponseStatus(HttpStatus.CREATED)
    TaxRateDto addTaxRate(
            @RequestBody @Valid NewTaxRateDto newTaxRateDto
    );


    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(
            summary = "Get all tax rates",
            description = "Retrieve a list of all tax rates."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tax rates retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaxRateDto[].class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string")
                    )
            )
    })
    @ResponseStatus(HttpStatus.OK)
    List<TaxRateDto> getAllTaxRates();


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/active")
    @Operation(
            summary = "Get active tax rates",
            description = "Retrieve all active tax rates."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Active tax rates retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaxRateDto[].class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string")
                    )
            )
    })
    @ResponseStatus(HttpStatus.OK)
    List<TaxRateDto> getActiveTaxRates();


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/default")
    @Operation(
            summary = "Get default tax rate",
            description = "Retrieve the default tax rate."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Default tax rate retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaxRateDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Default tax rate not found.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string")
                    )
            )
    })
    @ResponseStatus(HttpStatus.OK)
    TaxRateDto getDefaultTaxRate();


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(
            summary = "Update tax rate",
            description = "Update an existing tax rate. Admin is allowed."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tax rate updated successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaxRateDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid tax rate data.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Tax rate not found.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),

            @ApiResponse(
                    responseCode = "409",
                    description = "Tax rate conflict or operation forbidden by business rules.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )

    })
    @ResponseStatus(HttpStatus.OK)
    TaxRateDto updateTaxRate(
            @PathVariable Long id,
            @RequestBody @Valid NewTaxRateDto newTaxRateDto
    );


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete tax rate by ID",
            description = "Delete an existing tax rate. Admin is allowed."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Tax rate deleted successfully."
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string"))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Tax rate not found.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),

            @ApiResponse(
                    responseCode = "409",
                    description = "Tax rate conflict or operation forbidden by business rules.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )

    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteTaxRate(@PathVariable Long id);
}