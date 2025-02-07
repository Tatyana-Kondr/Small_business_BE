package de.ait.smallBusiness_be.sales.controllers.api;

import de.ait.smallBusiness_be.exceptions.ErrorResponseDto;
import de.ait.smallBusiness_be.sales.dto.NewSaleItemDto;
import de.ait.smallBusiness_be.sales.dto.SaleItemDto;
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
        @Tag(name = "Sale Item Controller")
)
@RequestMapping("/api/saleItems")
public interface SaleItemApi {

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{saleId}")
    @Operation(
            summary = "Add a new sale item in sale",
            description = "Create a new sale item. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Sale item created successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SaleItemDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid sale item data.",
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
    SaleItemDto addSaleItem(
            @RequestBody @Valid NewSaleItemDto newSaleItemDto,
            @PathVariable Long saleId);

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/sale/{saleId}")
    @Operation(
            summary = "Get all items of sale by sale_ID",
            description = "Retrieve a list of all items of sale.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "List of Sale's items retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SaleItemDto[].class))),
            @ApiResponse(responseCode = "404",
                    description = "No items found for sale with this id.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string")))
    })
    @ResponseStatus(HttpStatus.OK)
    List<SaleItemDto> getAllSaleItemsBySaleId(@PathVariable Long saleId);

    @GetMapping("/{saleId}/{saleItemId}")
    @Operation(
            summary = "Get sale item",
            description = "Retrieve a sale item by sale ID and item ID. Allowed to all users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Sale item retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SaleItemDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Sale item not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    SaleItemDto getSaleItemById(@PathVariable Long saleId, @PathVariable Long saleItemId);

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{saleId}/{saleItemId}")
    @Operation(
            summary = "Update the sale item",
            description = "Update the sale item by sale ID and item ID. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Sale item updated successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SaleItemDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid sale item data.",
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
    SaleItemDto updateSaleItem(
            @PathVariable Long saleId,
            @PathVariable Long saleItemId,
            @RequestBody @Valid NewSaleItemDto newSaleItemDto);

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{saleId}/{saleItemId}")
    @Operation(
            summary = "Delete sale item by sale ID and item ID",
            description = "Delete an existing sale item. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Sale item deleted successfully."),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Sale item not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteSaleItem(@PathVariable Long saleId,
                        @PathVariable Long saleItemId);
}
