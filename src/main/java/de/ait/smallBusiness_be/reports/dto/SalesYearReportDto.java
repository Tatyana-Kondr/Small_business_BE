package de.ait.smallBusiness_be.reports.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class SalesYearReportDto {

    private Integer year;

    private List<SalesMonthlyReportDto> months;

    private BigDecimal totalNet;
    private BigDecimal totalTax;
    private BigDecimal totalGross;
}
