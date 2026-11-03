package de.ait.smallBusiness_be.purchases.dao;

import de.ait.smallBusiness_be.purchases.model.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.stream.Stream;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long> {
    Stream<PurchaseItem> findByPurchaseId(Long purchaseId);

    @Query("""
        SELECT pi
        FROM PurchaseItem pi
        JOIN FETCH pi.purchase p
        WHERE pi.product.id = :productId
          AND p.purchasingDate = (
              SELECT MAX(p2.purchasingDate)
              FROM PurchaseItem pi2
              JOIN pi2.purchase p2
              WHERE pi2.product.id = :productId
          )
        ORDER BY pi.unitPrice DESC
        """)
    List<PurchaseItem> findLatestPurchaseItemsByProductId(
            @Param("productId") Long productId
    );

    boolean existsByProduct_Id(Long productId);
}
