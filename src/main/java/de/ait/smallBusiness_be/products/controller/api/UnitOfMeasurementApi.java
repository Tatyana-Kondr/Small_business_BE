package de.ait.smallBusiness_be.products.controller.api;

import de.ait.smallBusiness_be.exceptions.ErrorResponseDto;
import de.ait.smallBusiness_be.products.dto.NewUnitOfMeasurementDto;
import de.ait.smallBusiness_be.products.dto.UnitOfMeasurementDto;
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
        @Tag(name = "Unit Of Measurement controller")
)
@RequestMapping("/api/units")
public interface UnitOfMeasurementApi {
    
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @Operation(
            summary = "Add a new unit of measurement",
            description = "Create a new unit of measurement.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Unit of measurement created successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UnitOfMeasurementDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid unit of measurement data.",
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
    UnitOfMeasurementDto addUnitOfMeasurement(
            @RequestBody @Valid NewUnitOfMeasurementDto newUnit);


    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(
            summary = "Get all units of measurement.",
            description = "Retrieve a list of all units of measurement.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "List of units of measurement retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UnitOfMeasurementDto[].class))),
            @ApiResponse(responseCode = "404",
                    description = "No units of measurement found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string")))
    })
    @ResponseStatus(HttpStatus.OK)
    List<UnitOfMeasurementDto> getAllUnitOfMeasurements();


    @GetMapping("/{id}")
    @Operation(
            summary = "Get unit of measurement by ID",
            description = "Retrieve a unit of measurement by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Unit of measurement retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UnitOfMeasurementDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Unit of measurement not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    UnitOfMeasurementDto getUnitOfMeasurementById(@PathVariable Long id);


    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    @Operation(
            summary = "Update the unit of measurement",
            description = "Update the unit of measurement.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Unit of measurement updated successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UnitOfMeasurementDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid unit of measurement data.",
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
    UnitOfMeasurementDto updateUnitOfMeasurement(
            @PathVariable Long id,
            @RequestBody @Valid NewUnitOfMeasurementDto newUnitOfMeasurementDto);


    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete unit of measurement by ID",
            description = "Delete an existing unit of measurement.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Unit of measurement deleted successfully."),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Unit of measurement not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteUnitOfMeasurement(@PathVariable Long id);
}


