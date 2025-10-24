package de.ait.smallBusiness_be.products.dao;

import de.ait.smallBusiness_be.products.model.UnitOfMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitOfMeasurementRepository extends JpaRepository<UnitOfMeasurement, Long> {
    boolean existsByName(String name);
}
