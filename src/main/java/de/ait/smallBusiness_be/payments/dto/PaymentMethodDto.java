package de.ait.smallBusiness_be.payments.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "PaymentMethod", description = "Data of paymentMethod")
public class PaymentMethodDto {

    Long id;
    String provider;  // например, Visa, Mastercard, Альфа-Банк
    String maskedNumber;  // **** **** **** 1234
    String details;  // доп. информация (например, банк, срок действия)
    boolean active;
}
