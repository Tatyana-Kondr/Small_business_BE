package de.ait.smallBusiness_be.sales.dao;

import de.ait.smallBusiness_be.sales.models.SaleItem;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleItemRepository extends CrudRepository<SaleItem, Long> {

    List <SaleItem> findAllBySaleIdOrderByPosition(Long saleId);

}
