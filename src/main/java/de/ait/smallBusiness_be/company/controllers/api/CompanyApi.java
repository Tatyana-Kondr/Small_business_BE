package de.ait.smallBusiness_be.company.controllers.api;

import de.ait.smallBusiness_be.company.dto.CompanyDto;
import de.ait.smallBusiness_be.company.dto.NewCompanyDto;
import de.ait.smallBusiness_be.exceptions.ErrorResponseDto;
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
import org.springframework.web.multipart.MultipartFile;


@Tags(
        @Tag(name = "Company controller")
)
@RequestMapping("/api/companies")
public interface CompanyApi {

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(
            summary = "Add a new company",
            description = "Create a new company. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Company created successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CompanyDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid company data.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409",
                    description = "Company already exists.",
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
    CompanyDto createCompany(
            @RequestBody @Valid NewCompanyDto newCompanyDto);

    @GetMapping
    @Operation(
            summary = "Get company.",
            description = "Retrieve a company. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Company retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CompanyDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Company not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    CompanyDto getCompany();

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(
            summary = "Update the company",
            description = "Update the company. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Company updated successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CompanyDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid company data.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409",
                    description = "Company already exists.",
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
    CompanyDto updateCompany(
            @PathVariable Long id,
            @RequestBody @Valid NewCompanyDto newCompanyDto);

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/logo")
    @Operation(
            summary = "Upload or update company logo",
            description = "Upload or update the logo for the company. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Logo uploaded successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CompanyDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid file.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Company not found.",
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
    CompanyDto uploadLogo(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file);
}

