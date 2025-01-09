package de.ait.smallBusiness_be.products.controller.api;

import de.ait.smallBusiness_be.exceptions.ErrorResponseDto;
import de.ait.smallBusiness_be.products.dto.NewProductCategoryDto;
import de.ait.smallBusiness_be.products.dto.ProductCategoryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Product Category controller",
        description = "Controller for CRUD operations with the product category entity")
@RequestMapping("api/product-categories")
public interface ProductCategoryApi {

    //@PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    @Operation(
            summary = "Add a new Product category",
            description = "Create a new Product category. Only ADMINs are allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Product category created successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductCategoryDto.class))),
            @ApiResponse(responseCode = "409",
                    description = "Product Category with the same name already exists.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "403",
                    description = "User does not have the required role.",
                    content = @Content(mediaType = "*/*",
                            schema = @Schema(type = "string")))
    })
    @ResponseStatus(HttpStatus.CREATED)
    ProductCategoryDto createProductCategory(@RequestBody @Valid NewProductCategoryDto newCategory);

    @GetMapping
    @Operation(
            summary = "Get all categories",
            description = "Retrieve a list of all Product categories. Allowed to all users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "List of Product categories retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductCategoryDto[].class))),
            @ApiResponse(responseCode = "404",
                    description = "No categories found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    List<ProductCategoryDto> getAllProductCategories();

    //@PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    @Operation(
            summary = "Get Product category by ID",
            description = "Retrieve a Product category by its ID. Only authorized users are allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Product category retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductCategoryDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Category not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string")))
    })
    @ResponseStatus(HttpStatus.OK)
    ProductCategoryDto getProductCategoryById(@PathVariable int id);

    //@PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}")
    @Operation(
            summary = "Update Product category",
            description = "Update an existing Product category. Only ADMINs are allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Product category updated successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductCategoryDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Category not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409",
                    description = "Category with the same name already exists.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "403",
                    description = "User does not have the required role.",
                    content = @Content(mediaType = "*/*",
                            schema = @Schema(type = "string")))
    })
    @ResponseStatus(HttpStatus.OK)
    ProductCategoryDto updateProductCategoryById(@PathVariable int id, @RequestBody @Valid NewProductCategoryDto updatedCategory);

    //@PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete Product category",
            description = "Delete a Product category by its ID. Only ADMINs are allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Product category deleted successfully."),
            @ApiResponse(responseCode = "404",
                    description = "Category not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "403",
                    description = "User does not have the required role.",
                    content = @Content(mediaType = "*/*",
                            schema = @Schema(type = "string")))
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void removeProductCategoryById(@PathVariable int id);
}
