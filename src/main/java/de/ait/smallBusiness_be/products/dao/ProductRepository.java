package de.ait.smallBusiness_be.products.dao;

import de.ait.smallBusiness_be.products.dto.ProductPickDto;
import de.ait.smallBusiness_be.products.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * SmallBusiness_BE
 * 24.10.2024
 *
 * @author Kondratyeva
 */

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"productCategory", "unitOfMeasurement"})
    Page<Product> findByProductCategory_Id(Long id, Pageable pageable);

    @EntityGraph(attributePaths = {"productCategory", "unitOfMeasurement"})
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(p.article) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(p.vendorArticle) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
   Page<Product> searchProductsPage(@Param("searchTerm") String searchTerm, Pageable pageable);

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
    Page<Product> searchProductsByCategoryPage(@Param("categoryId") Long categoryId,
                                           @Param("searchTerm") String searchTerm,
                                           Pageable pageable);


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
        LOWER(TRIM(p.name)) = LOWER(TRIM(:name))
        OR (
            :vendorArticle IS NOT NULL
            AND TRIM(:vendorArticle) <> ''
            AND p.vendorArticle IS NOT NULL
            AND LOWER(TRIM(p.vendorArticle)) = LOWER(TRIM(:vendorArticle))
        )
    ORDER BY p.name, p.id
""")
    List<ProductPickDto> findSimilarProducts(
            @Param("name") String name,
            @Param("vendorArticle") String vendorArticle
    );

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
    WHERE p.id <> :productId
      AND (
          (
              :name IS NOT NULL
              AND LOWER(TRIM(p.name)) = LOWER(TRIM(:name))
          )
          OR (
              :article IS NOT NULL
              AND LOWER(TRIM(p.article)) = LOWER(TRIM(:article))
          )
          OR (
              :vendorArticle IS NOT NULL
              AND TRIM(:vendorArticle) <> ''
              AND p.vendorArticle IS NOT NULL
              AND LOWER(TRIM(p.vendorArticle)) = LOWER(TRIM(:vendorArticle))
          )
      )
    ORDER BY p.name, p.id
""")
    List<ProductPickDto> findSimilarProductsForUpdate(
            @Param("productId") Long productId,
            @Param("name") String name,
            @Param("article") String article,
            @Param("vendorArticle") String vendorArticle
    );

    boolean existsByProductCategory_Id(Long categoryId);
    boolean existsByUnitOfMeasurement_Id(Long unitId);
}
