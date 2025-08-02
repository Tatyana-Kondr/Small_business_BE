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
            summary = "Получить PDF счёт по номеру и году",
            description = "Возвращает PDF-файл счета, сгенерированного при продаже"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF счёт найден",
                    content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "404", description = "Счёт не найден",
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
            summary = "Получить PDF товарную накладную по номеру и году",
            description = "Возвращает PDF-файл накладной, сгенерированной при продаже"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF накладная найдена",
                    content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "404", description = "Накладная не найдена",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    ResponseEntity<Resource> getDeliveryBillPdf(
            @PathVariable String year,
            @PathVariable String deliveryBillNumber
    );
}
