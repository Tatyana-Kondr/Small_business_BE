package de.ait.smallBusiness_be.products.dao;


import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.products.model.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/**
 * SmallBusiness_BE
 * 24.10.2024
 *
 * @author Kondratyeva
 */

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByNameAndVendorArticleAndPurchasingPriceAndProductCategory(String name, String vendorArticle, BigDecimal purchasingPrice, ProductCategory productCategory);

    Page<Product> findByProductCategory_Id(int id, Pageable pageable);
}
