package de.ait.smallBusiness_be.purchases.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 15.01.2025
 * SmB
 *
 * @author Kondratyeva (AIT TR)
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "Purchase", description = "Data of purchase")
public class PurchaseDto {

    Long id;

    Long vendorId;

    LocalDate purchasingDate;

    String type;

    String document;

    String documentNumber;

    BigDecimal tax; // НДС в процентах

    BigDecimal subtotal; // Подитог

    BigDecimal taxSum; // Сумма налога

    BigDecimal total; // Общая сумма

    String paymentStatus;

    List<PurchaseItemDto> purchaseItems;
}
