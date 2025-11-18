package de.ait.smallBusiness_be.warehouse.controllers;

import de.ait.smallBusiness_be.exceptions.ErrorResponseDto;
import de.ait.smallBusiness_be.warehouse.dto.WarehouseRecordDto;
import de.ait.smallBusiness_be.warehouse.dto.WarehouseStockDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tags(
        @Tag(name = "Warehouse controller")
)
@RequestMapping("/api/warehouse")
public interface WarehouseApi {
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/stocks")
    @Operation(
            summary = "Get all stocks",
            description = "Retrieve a list of all stocks.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "List of stocks retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = WarehouseStockDto[].class))),
            @ApiResponse(responseCode = "404",
                    description = "No stocks found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string")))
    })
    @ResponseStatus(HttpStatus.OK)
    Page<WarehouseStockDto> getAllStocks(
            @PageableDefault(size = 15) Pageable pageable);

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/product/{productId}/stock")
    @Operation(
            summary = "Get stock by productID",
            description = "Retrieve a stock by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Stock retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = WarehouseStockDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Stock not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    WarehouseStockDto getProductStock(@PathVariable Long productId);

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/product/{productId}/history")
    @Operation(
            summary = "Get history of product",
            description = "Retrieve a history of product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "History retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = WarehouseRecordDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "History not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    Page<WarehouseRecordDto> getProductHistory(@PathVariable Long productId,
                                               @PageableDefault(size = 15) Pageable pageable);

}
