package de.ait.smallBusiness_be.productions.controllers.api;

import de.ait.smallBusiness_be.exceptions.ErrorResponseDto;
import de.ait.smallBusiness_be.productions.dto.NewProductionDto;
import de.ait.smallBusiness_be.productions.dto.ProductionDto;
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

@Tags(
        @Tag(name = "Production controller")
)
@RequestMapping("/api/productions")
public interface ProductionsApi {

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @Operation(
            summary = "Add a new production",
            description = "Create a new production. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Production created successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductionDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid production data.",
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
    ProductionDto addProduction(
            @RequestBody @Valid NewProductionDto newProductionDto);

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(
            summary = "Get all productions",
            description = "Retrieve a list of all productions.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "List of Productions retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductionDto[].class))),
            @ApiResponse(responseCode = "404",
                    description = "No productions found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string")))
    })
    @ResponseStatus(HttpStatus.OK)
    Page<ProductionDto> getAllProductions(
            @PageableDefault(size = 10, sort = "dateOfProduction", direction = Sort.Direction.DESC) Pageable pageable);

    @GetMapping("/{id}")
    @Operation(
            summary = "Get production by ID",
            description = "Retrieve a production by its ID. Allowed to all users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Production retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductionDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Production not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    ProductionDto getProductionById(@PathVariable Long id);

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    @Operation(
            summary = "Update the production",
            description = "Update the production. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Production updated successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductionDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid production data.",
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
    ProductionDto updateProduction(
            @PathVariable Long id,
            @RequestBody @Valid NewProductionDto newProductionDto);

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete production by ID",
            description = "Delete an existing production. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Production deleted successfully."),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Production not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void removeProduction(
            @PathVariable Long id);
}
