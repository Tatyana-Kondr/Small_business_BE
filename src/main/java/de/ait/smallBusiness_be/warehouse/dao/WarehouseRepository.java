package de.ait.smallBusiness_be.warehouse.dao;

import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.warehouse.models.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    Optional<Warehouse> findByProduct(Product product);

    List<Warehouse> findAllByProduct_IdIn(Collection<Long> productIds);
}
