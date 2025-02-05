package de.ait.smallBusiness_be.sales.dto;

import de.ait.smallBusiness_be.products.dto.NewDimensionsDto;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public record SaleDto(

        Long id,
        Long customerId,
        String invoiceNumber,
        String accountObject,
        String typeOfOperation,
        String shipping,
        NewDimensionsDto shippingDimensions,
        String termsOfPayment,
        LocalDate salesDate,
        LocalDate paymentDate,
        String paymentStatus,
        List<SaleItemDto> salesItems) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
