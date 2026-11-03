package de.ait.smallBusiness_be.products.service;

import de.ait.smallBusiness_be.products.dto.ProductPhotoDto;
import de.ait.smallBusiness_be.products.model.ProductPhoto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProductPhotoService {
    ProductPhotoDto uploadFile(Long productId, MultipartFile file);
    ProductPhotoDto replacePhoto(Long photoId, MultipartFile file);
    void reorderPhotos(Long productId, List<Long> photoIds);
    void deletePhoto(Long photoId);
    List<ProductPhotoDto> getPhotosByProductId(Long productId);
    List<ProductPhotoDto> getAllPhotos();
}
