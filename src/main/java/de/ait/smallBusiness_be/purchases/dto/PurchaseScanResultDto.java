package de.ait.smallBusiness_be.purchases.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseScanResultDto {

    private String vendorName;

    private String document;

    private String documentNumber;

    private LocalDate purchasingDate;
}
