package de.ait.smallBusiness_be.sales.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaxRateDto {

    private Long id;

    private BigDecimal value;

    private boolean active;

    private boolean defaultRate;
}
