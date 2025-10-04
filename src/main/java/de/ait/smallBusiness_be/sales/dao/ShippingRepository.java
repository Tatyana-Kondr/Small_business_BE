package de.ait.smallBusiness_be.sales.dao;

import de.ait.smallBusiness_be.sales.models.Shipping;
import org.springframework.data.jpa.repository.JpaRepository;



public interface ShippingRepository extends JpaRepository<Shipping, Long> {
    boolean existsByName(String name);
}
