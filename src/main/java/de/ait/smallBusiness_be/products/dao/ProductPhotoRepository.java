package de.ait.smallBusiness_be.products.dao;

import de.ait.smallBusiness_be.products.model.ProductPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProductPhotoRepository extends JpaRepository<ProductPhoto, Long> {

    @Query("""
        select p
        from ProductPhoto p
        where p.product.id = :productId
        order by
            case when p.position is null then 1 else 0 end,
            p.position asc,
            p.id asc
        """)
    List<ProductPhoto> findOrderedByProductId(@Param("productId") Long productId);

    @Query("""
        select max(p.position)
        from ProductPhoto p
        where p.product.id = :productId
        """)
    Integer findMaxPositionByProductId(@Param("productId") Long productId);

    @Query("""
    select distinct p.product.id
    from ProductPhoto p
    where p.product.id in :productIds
    """)
    List<Long> findProductIdsWithPhotos(
            @Param("productIds") Collection<Long> productIds
    );
}
