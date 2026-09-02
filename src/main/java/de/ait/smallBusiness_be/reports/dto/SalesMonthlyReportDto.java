package de.ait.smallBusiness_be.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class SalesMonthlyReportDto {

    private Integer year;
    private Integer month;

    private BigDecimal netAmount;
    private BigDecimal taxAmount;
    private BigDecimal grossAmount;

    public SalesMonthlyReportDto(
            Integer year,
            Integer month,
            BigDecimal netAmount,
            BigDecimal taxAmount,
            BigDecimal grossAmount
    ) {
        this.year = year;
        this.month = month;
        this.netAmount = netAmount;
        this.taxAmount = taxAmount;
        this.grossAmount = grossAmount;
    }
}
