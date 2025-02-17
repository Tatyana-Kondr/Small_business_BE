package de.ait.smallBusiness_be.productions.dao;

import de.ait.smallBusiness_be.productions.model.ProductionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductionItemRepository extends JpaRepository<ProductionItem, Long> {
}
