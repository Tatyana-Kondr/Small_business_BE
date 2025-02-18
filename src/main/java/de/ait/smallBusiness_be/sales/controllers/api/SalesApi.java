package de.ait.smallBusiness_be.sales.controllers.api;

import de.ait.smallBusiness_be.exceptions.ErrorResponseDto;
import de.ait.smallBusiness_be.sales.dto.NewSaleDto;
import de.ait.smallBusiness_be.sales.dto.NewSaleItemDto;
import de.ait.smallBusiness_be.sales.dto.SaleDto;
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

@Tags(
        @Tag(name = "Sale Controller")
)
@RequestMapping("/api/sales")
public interface SalesApi {

    @PreAuthorize("isAuthenticated()")
    @PostMapping()
    @Operation(
            summary = "Add a new sale.",
            description = "Create a new sale. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Sale created successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SaleDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid sale data.",
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
    SaleDto addSale(
            @RequestBody @Valid NewSaleDto newSaleDto);
}
