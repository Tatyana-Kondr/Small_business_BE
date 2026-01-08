package de.ait.smallBusiness_be.products.dao;

import de.ait.smallBusiness_be.products.dto.ProductPickDto;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.products.model.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * SmallBusiness_BE
 * 24.10.2024
 *
 * @author Kondratyeva
 */

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByNameAndVendorArticleAndPurchasingPriceAndProductCategory(String name, String vendorArticle, BigDecimal purchasingPrice, ProductCategory productCategory);

    @EntityGraph(attributePaths = {"productCategory", "unitOfMeasurement"})
    Page<Product> findByProductCategory_Id(int id, Pageable pageable);

    @EntityGraph(attributePaths = {"productCategory", "unitOfMeasurement"})
    List<Product> findAllByProductCategory_Id(int id);

    @EntityGraph(attributePaths = {"productCategory", "unitOfMeasurement"})
    Optional<Product> findProductByArticle(String article);

    @EntityGraph(attributePaths = {"productCategory", "unitOfMeasurement"})
    List<Product> findProductsByVendorArticle(String vendorArticle);

    @EntityGraph(attributePaths = {"productCategory", "unitOfMeasurement"})
    List<Product> findProductsByName(String name);

    @EntityGraph(attributePaths = {"productCategory", "unitOfMeasurement"})
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(p.article) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(p.vendorArticle) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
   Page<Product> searchProductsPage(@Param("searchTerm") String searchTerm, Pageable pageable);

    @EntityGraph(attributePaths = {"productCategory", "unitOfMeasurement"})
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(p.article) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(p.vendorArticle) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Product> searchProducts(@Param("searchTerm") String searchTerm);

    @EntityGraph(attributePaths = {"productCategory", "unitOfMeasurement"})
    @Query(
            value = """
            SELECT p FROM Product p
            WHERE p.productCategory.id = :categoryId
              AND (
                   LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                OR LOWER(p.article) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                OR LOWER(p.vendorArticle) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
              )
        """,
            countQuery = """
            SELECT COUNT(p) FROM Product p
            WHERE p.productCategory.id = :categoryId
              AND (
                   LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                OR LOWER(p.article) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                OR LOWER(p.vendorArticle) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
              )
        """
    )
    Page<Product> searchProductsByCategoryPage(@Param("categoryId") int categoryId,
                                           @Param("searchTerm") String searchTerm,
                                           Pageable pageable);

    @EntityGraph(attributePaths = {"productCategory", "unitOfMeasurement"})
    @Query("""
        SELECT p FROM Product p
        WHERE p.productCategory.id = :categoryId
          AND (
               LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(p.article) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(p.vendorArticle) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
          )
    """)
    List<Product> searchProductsByCategory(@Param("categoryId") int categoryId,
                                           @Param("searchTerm") String searchTerm);

    @Query("""
   SELECT new de.ait.smallBusiness_be.products.dto.ProductPickDto(
         p.id,
         p.name,
         p.article,
         p.vendorArticle,
         p.purchasingPrice,
         p.sellingPrice,
         p.productCategory.id
     )
    FROM Product p
    WHERE
        NOT (
            (:searchTerm IS NULL OR :searchTerm = '')
            AND :categoryId IS NULL
        )
        AND
        (:categoryId IS NULL OR p.productCategory.id = :categoryId)
        AND (
            :searchTerm IS NULL OR :searchTerm = '' OR
            LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR
            LOWER(p.article) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR
            LOWER(p.vendorArticle) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        )
    ORDER BY p.name
""")
    List<ProductPickDto> pickProducts(
            @Param("searchTerm") String searchTerm,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );


}
