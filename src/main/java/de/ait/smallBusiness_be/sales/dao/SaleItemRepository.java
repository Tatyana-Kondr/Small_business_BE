package de.ait.smallBusiness_be.sales.dao;

import de.ait.smallBusiness_be.sales.models.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    boolean existsByProduct_Id(Long productId);
}
