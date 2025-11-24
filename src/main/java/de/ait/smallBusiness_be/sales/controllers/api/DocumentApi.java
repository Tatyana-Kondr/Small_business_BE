package de.ait.smallBusiness_be.sales.controllers.api;

import de.ait.smallBusiness_be.exceptions.ErrorResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Tags({
        @Tag(name = "Invoice & Delivery Bill Controller")
})
@RequestMapping("/api/sales")
public interface DocumentApi {

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/invoices/{year}/{invoiceNumber}.pdf")
    @Operation(
            summary = "Get a PDF invoice by number and year.",
            description = "Returns a PDF file of the invoice generated during the sale."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF invoice found",
                    content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "404", description = "PDF invoice not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    ResponseEntity<Resource> getInvoicePdf(
            @PathVariable String year,
            @PathVariable String invoiceNumber
    );

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delivery-bill/{year}/{deliveryBillNumber}.pdf")
    @Operation(
            summary = "Get a PDF delivery-bill by number and year",
            description = "Returns the PDF file of the delivery-bill generated during the sale."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF delivery-bill found",
                    content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "404", description = "PDF delivery-bill not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    ResponseEntity<Resource> getDeliveryBillPdf(
            @PathVariable String year,
            @PathVariable String deliveryBillNumber
    );
}
