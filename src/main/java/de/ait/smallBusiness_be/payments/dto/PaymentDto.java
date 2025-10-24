package de.ait.smallBusiness_be.payments.dto;

import de.ait.smallBusiness_be.purchases.model.TypeOfDocument;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "Payment", description = "Data of payment")
public class PaymentDto {

    Long id;
    LocalDate paymentDate;
    Long customerId;
    String customerName;
    String type;
    BigDecimal amount;
    Long saleId;
    Long purchaseId;
    TypeOfDocument document;
    String documentNumber;
    Long paymentMethodId;
    Long paymentProcessId;
}
