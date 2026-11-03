package de.ait.smallBusiness_be.sales.controllers.api;

import de.ait.smallBusiness_be.sales.dto.DiscountRateDto;
import de.ait.smallBusiness_be.sales.dto.NewDiscountRateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/discount-rates")
@Tag(
        name = "Discount Rates",
        description = "API for managing discount rates"
)
public interface DiscountRateApi {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create discount rate")
    DiscountRateDto createDiscountRate(
            @Valid @RequestBody NewDiscountRateDto newDiscountRateDto
    );

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all discount rates")
    List<DiscountRateDto> getAllDiscountRates();

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get active discount rates")
    List<DiscountRateDto> getActiveDiscountRates();

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update discount rate")
    DiscountRateDto updateDiscountRate(
            @PathVariable Long id,
            @Valid @RequestBody NewDiscountRateDto newDiscountRateDto
    );

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete discount rate")
    void deleteDiscountRate(@PathVariable Long id);
}