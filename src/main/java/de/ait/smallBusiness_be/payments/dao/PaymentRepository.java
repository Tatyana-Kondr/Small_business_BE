package de.ait.smallBusiness_be.payments.dao;

import de.ait.smallBusiness_be.payments.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, PaymentRepositoryCustom {
    List<Payment> findByPurchaseId(Long purchaseId);
    List<Payment> findBySaleId(Long saleId);

    @Query("SELECT DISTINCT p.sale.id FROM Payment p")
    List<Long> findDistinctSaleIds();

    @Query("SELECT DISTINCT p.purchase.id FROM Payment p")
    List<Long> findDistinctPurchaseIds();
}
