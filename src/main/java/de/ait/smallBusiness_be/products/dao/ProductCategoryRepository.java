package de.ait.smallBusiness_be.products.dao;


import de.ait.smallBusiness_be.products.model.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * SmallBusiness_BE
 * 24.10.2024
 *
 * @author Kondratyeva
 */

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Integer> {

    List<ProductCategory> findByNameIgnoreCaseOrArtNameIgnoreCase(String name, String artName);
}
