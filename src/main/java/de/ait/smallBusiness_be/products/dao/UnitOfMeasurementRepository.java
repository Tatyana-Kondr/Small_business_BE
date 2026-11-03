package de.ait.smallBusiness_be.products.dao;

import de.ait.smallBusiness_be.products.model.UnitOfMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitOfMeasurementRepository
        extends JpaRepository<UnitOfMeasurement, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
