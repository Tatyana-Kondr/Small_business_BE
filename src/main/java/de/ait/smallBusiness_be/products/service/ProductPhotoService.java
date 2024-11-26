package de.ait.smallBusiness_be.products.service;

import de.ait.smallBusiness_be.products.model.ProductPhoto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProductPhotoService {
    String uploadFile(Long productId, MultipartFile file) throws IOException;

    void deletePhoto(Long photoId) throws IOException;

    List<ProductPhoto> getPhotosByProductId(Long productId);
}
