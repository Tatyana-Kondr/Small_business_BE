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

    @Query("""
    SELECT DISTINCT p.sale.id
    FROM Payment p
    WHERE p.sale IS NOT NULL
    """)
    List<Long> findDistinctSaleIds();

    @Query("""
    SELECT DISTINCT p.purchase.id
    FROM Payment p
    WHERE p.purchase IS NOT NULL
    """)
    List<Long> findDistinctPurchaseIds();

    boolean existsByPaymentProcess_Id(Long paymentProcessId);

    boolean existsByPaymentMethod_Id(Long paymentMethodId);

    boolean existsByDocument_Id(Long documentId);

    boolean existsBySaleId(Long saleId);

    boolean existsByPurchaseId(Long purchaseId);

    boolean existsByCustomer_Id(Long customerId);
}
