package de.ait.smallBusiness_be.purchases.dao;

import de.ait.smallBusiness_be.purchases.model.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;

public interface PurchaseRepositoryCustom {

    Page<Purchase> searchPurchases(Pageable pageable, String searchQuery);

    Page<Purchase> filterByFields(Pageable pageable, Long id, Long vendorId, String document, String documentNumber, BigDecimal total, String paymentStatus);
}
