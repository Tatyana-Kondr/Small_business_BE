package de.ait.smallBusiness_be.sales.dao;

import de.ait.smallBusiness_be.sales.models.TaxRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaxRateRepository extends JpaRepository<TaxRate, Long> {

    List<TaxRate> findAllByOrderByValueAsc();

    List<TaxRate> findByActiveTrueOrderByValueAsc();

    Optional<TaxRate> findByDefaultRateTrue();

    boolean existsByValue(java.math.BigDecimal value);
}
