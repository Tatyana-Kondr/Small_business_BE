package de.ait.smallBusiness_be.sales.dao;

import de.ait.smallBusiness_be.sales.models.DiscountRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface DiscountRateRepository extends JpaRepository<DiscountRate, Long> {

    List<DiscountRate> findAllByOrderByValueAsc();

    List<DiscountRate> findByActiveTrueOrderByValueAsc();

    boolean existsByValue(BigDecimal value);
}
