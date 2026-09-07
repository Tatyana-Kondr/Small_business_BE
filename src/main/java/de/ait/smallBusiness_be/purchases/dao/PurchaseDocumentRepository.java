package de.ait.smallBusiness_be.purchases.dao;

import de.ait.smallBusiness_be.purchases.model.PurchaseDocument;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface PurchaseDocumentRepository extends JpaRepository<PurchaseDocument, Long> {

    List<PurchaseDocument> findAllByPurchaseIdOrderByIdAsc(Long purchaseId);
}
