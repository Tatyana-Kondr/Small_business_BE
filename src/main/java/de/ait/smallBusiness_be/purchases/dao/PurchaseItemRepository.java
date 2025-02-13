package de.ait.smallBusiness_be.purchases.dao;

import de.ait.smallBusiness_be.purchases.model.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.stream.Stream;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long> {
    Stream<PurchaseItem> findByPurchaseId(Long purchaseId);

    @Query("SELECT COALESCE(MAX(p.position), 0) FROM PurchaseItem p WHERE p.purchase.id = :purchaseId")
    Integer findMaxPositionByPurchaseId(@Param("purchaseId") Long purchaseId);
}
