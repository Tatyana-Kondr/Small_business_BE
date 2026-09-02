package de.ait.smallBusiness_be.sales.dao;

import de.ait.smallBusiness_be.reports.dto.SalesMonthlyReportDto;
import de.ait.smallBusiness_be.sales.models.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long>, SaleRepositoryCustom {

        boolean existsByInvoiceNumber(String invoiceNumber);

        @Query("SELECT MAX(CAST(SUBSTRING(s.invoiceNumber, 7, 3) AS int)) FROM Sale s WHERE FUNCTION('YEAR', s.salesDate) = :year")
        Integer findLastInvoiceSequenceForYear(@Param("year") int year);

        @Query("""
    SELECT new de.ait.smallBusiness_be.reports.dto.SalesMonthlyReportDto(
        year(s.salesDate),
        month(s.salesDate),
        SUM(s.totalPrice),
        SUM(s.taxAmount),
        SUM(s.totalAmount)
    )
    FROM Sale s
    WHERE s.salesDate IS NOT NULL
    GROUP BY
        year(s.salesDate),
        month(s.salesDate)
    ORDER BY
        year(s.salesDate),
        month(s.salesDate)
""")
        List<SalesMonthlyReportDto> getSalesMonthlyReport();

        @Query("""
    SELECT new de.ait.smallBusiness_be.reports.dto.SalesMonthlyReportDto(
        year(s.salesDate),
        month(s.salesDate),
        SUM(s.totalPrice),
        SUM(s.taxAmount),
        SUM(s.totalAmount)
    )
    FROM Sale s
    WHERE s.salesDate IS NOT NULL
      AND year(s.salesDate) = :year
    GROUP BY
        year(s.salesDate),
        month(s.salesDate)
    ORDER BY
        month(s.salesDate)
""")
        List<SalesMonthlyReportDto> getSalesMonthlyReportByYear(
                @Param("year") Integer year
        );

}
