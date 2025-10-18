package de.ait.smallBusiness_be;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.products.dao.ProductPhotoRepository;
import de.ait.smallBusiness_be.products.dao.ProductRepository;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.products.model.ProductPhoto;
import de.ait.smallBusiness_be.products.service.impl.ProductPhotoServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ProductPhotoServiceImplTest {

    @InjectMocks
    private ProductPhotoServiceImpl service;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductPhotoRepository productPhotoRepository;

    @Value("${file.photo-dir}")
    private String photoDir = "photos";

    @Value("${file.document-dir}")
    private String documentDir = "documents";

    @Test
    void uploadFile_success_photo() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        Product product = new Product();
        ProductPhoto savedPhoto = new ProductPhoto();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(file.getOriginalFilename()).thenReturn("image.jpg");
        when(productPhotoRepository.save(any(ProductPhoto.class))).thenReturn(savedPhoto);

        ProductPhoto result = service.uploadFile(1L, file);

        assertThat(result).isNotNull();
        verify(productPhotoRepository, times(2)).save(any(ProductPhoto.class));
    }

    @Test
    void uploadFile_productNotFound_throws() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.uploadFile(1L, file))
                .isInstanceOf(RestApiException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void deletePhoto_success() throws IOException {
        ProductPhoto photo = new ProductPhoto();
        photo.setOriginFileName("file.jpg");

        when(productPhotoRepository.findById(1L)).thenReturn(Optional.of(photo));

        service.deletePhoto(1L);

        verify(productPhotoRepository).delete(photo);
    }

    @Test
    void deletePhoto_notFound_throws() {
        when(productPhotoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deletePhoto(1L))
                .isInstanceOf(RestApiException.class)
                .hasMessageContaining("Photo not found");
    }
}

