package de.ait.smallBusiness_be.payments.dao;

import de.ait.smallBusiness_be.payments.model.PaymentProcess;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PaymentProcessRepository extends JpaRepository<PaymentProcess, Long> {
    boolean existsByProcessName(String name);
}
