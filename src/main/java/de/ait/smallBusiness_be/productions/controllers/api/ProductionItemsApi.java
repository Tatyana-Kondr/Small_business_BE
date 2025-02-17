package de.ait.smallBusiness_be.productions.controllers.api;

import de.ait.smallBusiness_be.exceptions.ErrorResponseDto;
import de.ait.smallBusiness_be.productions.dto.NewProductionItemDto;
import de.ait.smallBusiness_be.productions.dto.ProductionDto;
import de.ait.smallBusiness_be.productions.dto.ProductionItemDto;
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
        @Tag(name = "ProductionItem controller")
)
@RequestMapping("/api/productionItems")
public interface ProductionItemsApi {

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{productionId}")
    @Operation(
            summary = "Add a new productionItem",
            description = "Create a new productionItem. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "ProductionItem created successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductionDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid productionItem data.",
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
    ProductionItemDto addProductionItem(
            @RequestBody @Valid NewProductionItemDto newProductionItemDto,
            @PathVariable Long productionId);

    @GetMapping("/{id}")
    @Operation(
            summary = "Get productionItem by ID",
            description = "Retrieve a productionItem by its ID. Allowed to all users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "ProductionItem retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductionDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "ProductionItem not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    ProductionItemDto getProductionItemById(@PathVariable Long id);

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    @Operation(
            summary = "Update the productionItem",
            description = "Update the productionItem. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "ProductionItem updated successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductionDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid productionItem data.",
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
    ProductionItemDto updateProductionItem(
            @PathVariable Long id,
            @RequestBody @Valid NewProductionItemDto newProductionItemDto);

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete productionItem by ID",
            description = "Delete an existing productionItem. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "ProductionItem deleted successfully."),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "ProductionItem not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void removeProductionItem(
            @PathVariable Long id);
}
