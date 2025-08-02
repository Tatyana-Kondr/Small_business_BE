package de.ait.smallBusiness_be.sales.dao;

import de.ait.smallBusiness_be.sales.models.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long>, SaleRepositoryCustom {

        boolean existsByInvoiceNumber(String invoiceNumber);

        @Query("SELECT MAX(CAST(SUBSTRING(s.invoiceNumber, 10, 4) AS int)) FROM Sale s WHERE FUNCTION('YEAR', s.salesDate) = :year")
        Integer findLastInvoiceSequenceForYear(@Param("year") int year);

}
