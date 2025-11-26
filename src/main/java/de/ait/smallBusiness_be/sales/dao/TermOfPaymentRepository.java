package de.ait.smallBusiness_be.sales.dao;

import de.ait.smallBusiness_be.sales.models.TermOfPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TermOfPaymentRepository extends JpaRepository<TermOfPayment, Long> {
    boolean existsByName(String name);
    Optional<TermOfPayment> findByName(String name);
}
