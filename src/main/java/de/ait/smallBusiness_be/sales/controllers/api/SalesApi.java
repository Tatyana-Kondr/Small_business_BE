package de.ait.smallBusiness_be.sales.controllers.api;

import de.ait.smallBusiness_be.exceptions.ErrorResponseDto;
import de.ait.smallBusiness_be.sales.dto.NewSaleDto;
import de.ait.smallBusiness_be.sales.dto.SaleDto;
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
import java.time.LocalDate;

@Tags(
        @Tag(name = "Sale Controller")
)
@RequestMapping("/api/sales")
public interface SalesApi {

    @PreAuthorize("isAuthenticated()")
    @PostMapping()
    @Operation(
            summary = "Add a new sale.",
            description = "Create a new sale.")
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


    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(
            summary = "Get all sales",
            description = "Retrieve a list of all sales.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "List of Sales retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SaleDto[].class))),
            @ApiResponse(responseCode = "404",
                    description = "No sales found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string")))
    })
    @ResponseStatus(HttpStatus.OK)
    Page<SaleDto> getAllSales(
            @PageableDefault(size = 10, sort = "salesDate", direction = Sort.Direction.DESC) Pageable pageable);


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    @Operation(
            summary = "Get sale by ID",
            description = "Retrieve a sale by its ID. Allowed to all users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Sale retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SaleDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Sale not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    SaleDto getSaleById(@PathVariable Long id);

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/search/{query}")
    @Operation(
            summary = "Get all sales according to search parameters",
            description = "Retrieve a list of all sales.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "List of Sales retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SaleDto[].class))),
            @ApiResponse(responseCode = "404",
                    description = "No sales found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string")))
    })
    @ResponseStatus(HttpStatus.OK)
    Page<SaleDto> searchSales(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(defaultValue = "salesDate") String sort,
            @PathVariable String query);

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/filter")
    @Operation(
            summary = "Get all sales according to the filters",
            description = "Retrieve a list of all sales.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "List of Sales retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SaleDto[].class))),
            @ApiResponse(responseCode = "404",
                    description = "No sales found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string")))
    })
    @ResponseStatus(HttpStatus.OK)
    Page<SaleDto> getAllSalesByFilter(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(defaultValue = "salesDate") String sort,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String invoiceNumber,
            @RequestParam(required = false) BigDecimal totalAmount,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false)LocalDate endDate,
            @RequestParam(required = false) String searchQuery);

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    @Operation(
            summary = "Update the sale",
            description = "Update the sale.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Sale updated successfully.",
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
    @ResponseStatus(HttpStatus.OK)
    SaleDto updateSale(
            @PathVariable Long id,
            @RequestBody @Valid NewSaleDto newSaleDto);

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete sale by ID",
            description = "Delete an existing sale.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Sale deleted successfully."),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Sale not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteSale(
            @PathVariable Long id);

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}/update-payment-status")
    @Operation(
            summary = "Update sale payment status",
            description = "Update the payment status (e.g., TEILWEISEBEZAHLT or BEZAHLT) for a specific sale.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Payment status updated successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SaleDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid sale ID.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404",
                    description = "Sale not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    SaleDto updatePaymentStatus(
            @PathVariable Long id);

}
