package de.ait.smallBusiness_be.products.dao;


import de.ait.smallBusiness_be.products.model.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * SmallBusiness_BE
 * 24.10.2024
 *
 * @author Kondratyeva
 */

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByArtNameIgnoreCase(String artName);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    boolean existsByArtNameIgnoreCaseAndIdNot(String artName, Long id);
}
