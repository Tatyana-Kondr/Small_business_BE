package de.ait.smallBusiness_be.payments.dao;

import de.ait.smallBusiness_be.payments.model.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    boolean existsPaymentMethodByProviderAndMaskedNumber(String provider, String maskedNumber);
    List<PaymentMethod> findAllByActive(boolean active);
}
