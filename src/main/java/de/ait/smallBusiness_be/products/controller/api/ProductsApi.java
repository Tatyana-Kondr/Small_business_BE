package de.ait.smallBusiness_be.products.controller.api;

import de.ait.smallBusiness_be.customers.dto.CustomerDto;
import de.ait.smallBusiness_be.customers.dto.NewCustomerDto;
import de.ait.smallBusiness_be.exceptions.ErrorResponseDto;
import de.ait.smallBusiness_be.products.dto.NewProductDto;
import de.ait.smallBusiness_be.products.dto.ProductDto;
import de.ait.smallBusiness_be.products.dto.UpdateProductDto;
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
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tags(
        @Tag(name = "Products")
)
@RequestMapping("/api/products")
public interface ProductsApi {
    //@PreAuthorize("isAuthenticated()")
    @PostMapping
    @Operation(
            summary = "Add a new product",
            description = "Create a new product. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Product created successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid product data.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409",
                    description = "Product with the same name and article already exists.",
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
    ProductDto createProduct(
            @RequestBody @Valid NewProductDto newProductDto
            //@Parameter(hidden = true)
            //@AuthenticationPrincipal AuthenticatedUser currentUser
    );

    @GetMapping("/{id}")
    @Operation(
            summary = "Get product by ID",
            description = "Retrieve a product by its ID. Allowed to all users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Product retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Product not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    ProductDto getProductById(@PathVariable Long id);

    //@PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    @Operation(
            summary = "Update product by ID",
            description = "Update an existing product. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Product updated successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid product data.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Product not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409",
                    description = "Product with the same name and article already exists.",
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
    ProductDto updateProductById(
            @PathVariable Long id,
            @RequestBody @Valid UpdateProductDto updateProductDto
//            , @Parameter(hidden = true)
//            @AuthenticationPrincipal AuthenticatedUser currentUser
    );


    //@PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete product by ID",
            description = "Delete an existing product. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Product deleted successfully."),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Product not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void removeProductById(
            @PathVariable Long id
//           , @Parameter(hidden = true)
//            @AuthenticationPrincipal AuthenticatedUser currentUser
    );

    @GetMapping
    @Operation(
            summary = "Get all products",
            description = "Retrieve a list of all products. Allowed to all users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "List of products retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductDto[].class))),
            @ApiResponse(responseCode = "404",
                    description = "No products found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    Page<ProductDto> getAllProducts(
            @PageableDefault(size = 10, sort = "name") Pageable pageable);

    @GetMapping("/category/{category-id}")
    @Operation(
            summary = "Get all products by category",
            description = "Retrieve a list of all products by category. Allowed to all users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "List of products retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductDto[].class))),
            @ApiResponse(responseCode = "404",
                    description = "No products found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    Page<ProductDto> getProductsByCategory(
            @PathVariable("category-id") int categoryId,
            @PageableDefault(size = 10, sort = "name") Pageable pageable
            );

}
