package de.ait.smallBusiness_be.payments.controllers.api;

import de.ait.smallBusiness_be.exceptions.ErrorResponseDto;
import de.ait.smallBusiness_be.payments.dto.NewPaymentDto;
import de.ait.smallBusiness_be.payments.dto.PaymentDto;
import de.ait.smallBusiness_be.payments.dto.PaymentPrefillDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import java.util.List;

@Tags(
        @Tag(name = "Payment controller")
)
@RequestMapping("/api/payments")
public interface PaymentApi {
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @Operation(
            summary = "Add a new payment",
            description = "Create a new payment.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Payment created successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid payment data.",
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
    PaymentDto addPayment(
            @RequestBody @Valid NewPaymentDto newPaymentDto);


    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(
            summary = "Get all payments.",
            description = "Retrieve a list of all payments.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "List of payments retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentDto[].class))),
            @ApiResponse(responseCode = "404",
                    description = "No payments found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string")))
    })
    @ResponseStatus(HttpStatus.OK)
    Page<PaymentDto> getAllPayments( @PageableDefault(size = 15, sort = {"paymentDate"},
            direction = Sort.Direction.DESC) Pageable pageable);

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/search/{query}")
    @Operation(
            summary = "Get all payments according to search parameters",
            description = "Retrieve a list of all payments.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "List of payments retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentDto[].class))),
            @ApiResponse(responseCode = "404",
                    description = "No payments found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string")))
    })
    @ResponseStatus(HttpStatus.OK)
    Page<PaymentDto> searchPayments(
            @PageableDefault(size = 15) Pageable pageable,
            @RequestParam(defaultValue = "paymentDate") String sort,
            @PathVariable String query);

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/filter")
    @Operation(
            summary = "Get all payments according to the filters",
            description = "Retrieve a list of all payments.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "List of payments retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentDto[].class))),
            @ApiResponse(responseCode = "404",
                    description = "No payments found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string")))
    })
    @ResponseStatus(HttpStatus.OK)
    Page<PaymentDto> getAllPaymentsByFilter(
            @PageableDefault(size = 15) Pageable pageable,
            @RequestParam(defaultValue = "paymentDate") String sort,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) Long saleId,
            @RequestParam(required = false) Long purchaseId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false)LocalDate endDate,
            @RequestParam(required = false) Long documentId,
            @RequestParam(required = false) String documentNumber,
            @RequestParam(required = false) BigDecimal amount,
            @RequestParam(required = false) String searchQuery);


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    @Operation(
            summary = "Get payment by ID",
            description = "Retrieve a payment by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Payment retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Payment not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    PaymentDto getPaymentById(@PathVariable Long id);


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(
            summary = "Update the payment.",
            description = "Update the payment. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Payment updated successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid payment data.",
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
    PaymentDto updatePayment(
            @PathVariable Long id,
            @RequestBody @Valid NewPaymentDto newPaymentDto);


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete payment by ID",
            description = "Delete an existing payment. Admin is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Payment deleted successfully."),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Payment not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deletePayment(@PathVariable Long id);

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/prefill/sale/{saleId}")
    @Operation(
            summary = "Get prefilled payment data for a sale",
            description = "Returns customer and document data based on the sale ID for form pre-filling."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Prefilled data retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentPrefillDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Sale not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    PaymentPrefillDto getPrefillDataForSale(@PathVariable Long saleId);


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/prefill/purchase/{purchaseId}")
    @Operation(
            summary = "Get prefilled payment data for a purchase",
            description = "Returns customer and document data based on the purchase ID for form pre-filling."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Prefilled data retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentPrefillDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Purchase not found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    PaymentPrefillDto getPrefillDataForPurchase(@PathVariable Long purchaseId);

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/all-sale-ids")
    @Operation(
            summary = "Get all unique sale IDs",
            description = "Retrieve a list of all unique sale IDs.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "List of sale IDs retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Long.class)))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string")))
    })
    @ResponseStatus(HttpStatus.OK)
    List<Long> getAllSaleIds();

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/all-purchase-ids")
    @Operation(
            summary = "Get all unique purchase IDs",
            description = "Retrieve a list of all unique purchase IDs.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "List of purchase IDs retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Long.class)))),
            @ApiResponse(responseCode = "401",
                    description = "User unauthorized.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string")))
    })
    @ResponseStatus(HttpStatus.OK)
    List<Long> getAllPurchaseIds();


}
