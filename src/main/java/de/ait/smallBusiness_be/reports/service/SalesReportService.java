package de.ait.smallBusiness_be.reports.service;

import de.ait.smallBusiness_be.reports.dto.SalesMonthlyReportDto;
import de.ait.smallBusiness_be.reports.dto.SalesYearReportDto;
import de.ait.smallBusiness_be.sales.dao.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesReportService {

    private final SaleRepository saleRepository;

    public List<SalesYearReportDto> getSalesReport(Integer year) {

        List<SalesMonthlyReportDto> monthlyReports =
                year == null
                        ? saleRepository.getSalesMonthlyReport()
                        : saleRepository.getSalesMonthlyReportByYear(year);

        return monthlyReports.stream()
                .collect(Collectors.groupingBy(
                        SalesMonthlyReportDto::getYear,
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .entrySet()
                .stream()
                .map(entry -> {

                    List<SalesMonthlyReportDto> months = entry.getValue();

                    BigDecimal totalNet = months.stream()
                            .map(SalesMonthlyReportDto::getNetAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal totalTax = months.stream()
                            .map(SalesMonthlyReportDto::getTaxAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal totalGross = months.stream()
                            .map(SalesMonthlyReportDto::getGrossAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return SalesYearReportDto.builder()
                            .year(entry.getKey())
                            .months(months)
                            .totalNet(totalNet)
                            .totalTax(totalTax)
                            .totalGross(totalGross)
                            .build();
                })
                .toList();
    }
}
