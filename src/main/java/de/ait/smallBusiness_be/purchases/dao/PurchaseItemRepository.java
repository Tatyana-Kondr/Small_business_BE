package de.ait.smallBusiness_be.purchases.dao;

import de.ait.smallBusiness_be.purchases.model.PurchaseItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long> {
    Page<PurchaseItem> findByPurchaseId(Pageable pageable, Long purchaseId);

    @Query("SELECT COALESCE(MAX(p.position), 0) FROM PurchaseItem p WHERE p.purchase.id = :purchaseId")
    Integer findMaxPositionByPurchaseId(@Param("purchaseId") Long purchaseId);
}
