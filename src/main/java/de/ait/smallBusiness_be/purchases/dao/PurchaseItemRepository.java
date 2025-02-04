package de.ait.smallBusiness_be.purchases.dao;

import de.ait.smallBusiness_be.purchases.model.PurchaseItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long> {
    Page<PurchaseItem> findByPurchaseId(Pageable pageable, Long purchaseId);
}
