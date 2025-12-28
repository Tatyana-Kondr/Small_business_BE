package de.ait.smallBusiness_be.products.controller.api;

import de.ait.smallBusiness_be.exceptions.ErrorResponseDto;
import de.ait.smallBusiness_be.products.dto.NewProductDto;
import de.ait.smallBusiness_be.products.dto.ProductDto;
import de.ait.smallBusiness_be.products.dto.UpdateProductDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.validation.Valid;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tags(
        @Tag(name = "Product controller")
)
@RequestMapping("/api/products")
public interface ProductsApi {

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @Operation(
            summary = "Add a new product",
            description = "Create a new product. Only authorized users are allowed.")
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
    ProductDto createProduct (@RequestBody @Valid NewProductDto newProductDto);


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    @Operation(
            summary = "Get product by ID",
            description = "Retrieve a product by its ID. Only authorized users are allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Product retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Product not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string")))
    })
    @ResponseStatus(HttpStatus.OK)
    ProductDto getProductById(@PathVariable Long id);


    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    @Operation(
            summary = "Update product by ID",
            description = "Update an existing product. Only authorized users are allowed.")
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
    ProductDto updateProductById(@PathVariable Long id,
                                 @RequestBody @Valid UpdateProductDto updateProductDto);


    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete product by ID",
            description = "Delete an existing product. Only authorized users are allowed.")
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
    void removeProductById(@PathVariable Long id);


    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(
            summary = "Get or search products",
            description = "Returns a page of products. If search term is provided, filters by it. Only authorized users are allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "List of products retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404",
                    description = "No products found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    Page<ProductDto> getProductsPaged(
            @Parameter(description = "Search term for filtering products by name, article or vendor article", example = "wasser")
            @RequestParam(required = false) String search,

            @ParameterObject
            @PageableDefault(size = 15, sort = "name") Pageable pageable);

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/all")
    @Operation(
            summary = "Get or search products",
            description = "Returns a list of products. If search term is provided, filters by it. Only authorized users are allowed.")
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
    List<ProductDto> getAllProducts(
            @Parameter(description = "Search term for filtering products by name, article or vendor article", example = "wasser")
            @RequestParam(required = false) String search);


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/category/{categoryId}")
    @Operation(
            summary = "Get all products by category",
            description = "Retrieve a list of all products by category. Only authorized users are allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "List of products retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404",
                    description = "No products found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    Page<ProductDto> getProductsByCategoryPaged(@PathVariable("categoryId") int categoryId,
                                           @RequestParam(name = "search", required = false) String search,
                                           @PageableDefault(size = 15, sort = "name") Pageable pageable);

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/category/{categoryId}/all")
    @Operation(
            summary = "Get all products by category",
            description = "Retrieve a list of all products by category. Only authorized users are allowed.")
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
    List<ProductDto> getProductsByCategory(@PathVariable("categoryId") int categoryId,
                                                @RequestParam(name = "search", required = false) String search);
}