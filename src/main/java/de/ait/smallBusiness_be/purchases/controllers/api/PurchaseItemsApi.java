package de.ait.smallBusiness_be.purchases.controllers.api;

import de.ait.smallBusiness_be.exceptions.ErrorResponseDto;
import de.ait.smallBusiness_be.purchases.dto.NewPurchaseItemDto;
import de.ait.smallBusiness_be.purchases.dto.PurchaseDto;
import de.ait.smallBusiness_be.purchases.dto.PurchaseItemDto;
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
        @Tag(name = "Purchase Item controller")
)
@RequestMapping("/api/purchaseItems")
public interface PurchaseItemsApi {

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{purchaseId}")
    @Operation(
            summary = "Add a new purchase item",
            description = "Create a new purchase item. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Purchase item created successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PurchaseDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid purchase item data.",
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
    PurchaseItemDto addPurchaseItem(
            @RequestBody @Valid NewPurchaseItemDto newPurchaseItemDto,
            @PathVariable Long purchaseId);

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/purchase/{purchaseId}")
    @Operation(
            summary = "Get all items of purchase by purchase_ID ",
            description = "Retrieve a list of all items of purchase.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "List of Purchase's items retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PurchaseItemDto[].class))),
            @ApiResponse(responseCode = "404",
                    description = "No items found for purchase with this id.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string")))
    })
    @ResponseStatus(HttpStatus.OK)
    List<PurchaseItemDto> getAllPurchaseItemsByPurchaseId(
            @PathVariable Long purchaseId);

    @GetMapping("/{id}")
    @Operation(
            summary = "Get purchase item by ID",
            description = "Retrieve a purchase item by its ID. Allowed to all users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Purchase item retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PurchaseDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "purchase item not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    PurchaseItemDto getPurchaseItemById(@PathVariable Long id);



    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    @Operation(
            summary = "Update the purchase item",
            description = "Update the purchase item. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Purchase item updated successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PurchaseDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid purchase item data.",
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
    PurchaseItemDto updatePurchaseItem(
            @PathVariable Long id,
            @RequestBody @Valid NewPurchaseItemDto newPurchaseItemDto);

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete purchase item by ID",
            description = "Delete an existing purchase item. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Purchase item deleted successfully."),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Purchase item not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deletePurchaseItem(
            @PathVariable Long id);

}
