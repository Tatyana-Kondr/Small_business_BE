package de.ait.smallBusiness_be.purchases.controllers.api;

import de.ait.smallBusiness_be.exceptions.ErrorResponseDto;
import de.ait.smallBusiness_be.purchases.dto.NewTypeOfDocumentDto;
import de.ait.smallBusiness_be.purchases.dto.TypeOfDocumentDto;
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
        @Tag(name = "Type Of Document controller")
)
@RequestMapping("/api/document-types")
public interface TypeOfDocumentApi {

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @Operation(
            summary = "Add a new type of document",
            description = "Create a new unit of measurement.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Type of document created successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TypeOfDocumentDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid type of document data.",
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
    TypeOfDocumentDto addTypeOfDocument(
            @RequestBody @Valid NewTypeOfDocumentDto newType);


    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(
            summary = "Get all types of document.",
            description = "Retrieve a list of all types of document.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "List of types of document retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TypeOfDocumentDto[].class))),
            @ApiResponse(responseCode = "404",
                    description = "No types of document found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string")))
    })
    @ResponseStatus(HttpStatus.OK)
    List<TypeOfDocumentDto> getAllTypesOfDocument();


    @GetMapping("/{id}")
    @Operation(
            summary = "Get type of document by ID",
            description = "Retrieve a type of document by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Type of document retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TypeOfDocumentDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Type of document not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    TypeOfDocumentDto getTypeOfDocumentById(@PathVariable Long id);


    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    @Operation(
            summary = "Update the type of document",
            description = "Update the type of document.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Type of document updated successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TypeOfDocumentDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid type of document data.",
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
    TypeOfDocumentDto updateTypeOfDocument(
            @PathVariable Long id,
            @RequestBody @Valid NewTypeOfDocumentDto newType);


    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete type of document by ID",
            description = "Delete an existing type of document.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Type of document deleted successfully."),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Type of document not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteTypeOfDocument(@PathVariable Long id);
}


