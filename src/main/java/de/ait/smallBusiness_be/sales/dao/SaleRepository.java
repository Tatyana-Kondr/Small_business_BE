package de.ait.smallBusiness_be.sales.dao;

import de.ait.smallBusiness_be.sales.models.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

}
