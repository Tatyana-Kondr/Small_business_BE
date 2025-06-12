package de.ait.smallBusiness_be.sales.dao;

import de.ait.smallBusiness_be.sales.models.SaleItem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleItemRepository extends CrudRepository<SaleItem, Long> {

    List <SaleItem> findAllBySaleIdOrderByPosition(Long saleId);

    @Query("SELECT COALESCE(MAX(s.position), 0) FROM SaleItem s WHERE s.sale.id = :saleId")
    Integer findMaxPositionBySaleId(@Param("saleId")Long saleId);
}
