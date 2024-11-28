package de.ait.smallBusiness_be.products.dao;

import de.ait.smallBusiness_be.products.model.ProductPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductPhotoRepository extends JpaRepository<ProductPhoto, Long> {

    List<ProductPhoto> findByProductId(Long productId);
}
