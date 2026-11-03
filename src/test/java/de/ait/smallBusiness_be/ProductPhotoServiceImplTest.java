package de.ait.smallBusiness_be;

import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.products.dao.ProductPhotoRepository;
import de.ait.smallBusiness_be.products.dao.ProductRepository;
import de.ait.smallBusiness_be.products.dto.ProductPhotoDto;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.products.model.ProductPhoto;
import de.ait.smallBusiness_be.products.service.impl.ProductPhotoServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductPhotoServiceImplTest {
    @InjectMocks private ProductPhotoServiceImpl service;
    @Mock private ProductRepository productRepository;
    @Mock private ProductPhotoRepository productPhotoRepository;
    @TempDir Path tempDir;

    private Path uploadRoot;
    private Path productFolder;
    private Product product;

    @BeforeEach
    void setUp() {
        uploadRoot = tempDir.resolve("uploads");
        productFolder = uploadRoot.resolve("products/UNCATEGORIZED/1_TEST");
        ReflectionTestUtils.setField(service, "uploadRoot", uploadRoot.toString());
        ReflectionTestUtils.setField(service, "productsDir", uploadRoot.resolve("products").toString());
        product = new Product();
        product.setId(1L);
        product.setVendorArticle("TEST");
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private MockMultipartFile file(String name) {
        return new MockMultipartFile("file", name, "application/octet-stream", "file content".getBytes());
    }

    private void finishTransaction(int status) {
        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
        if (status == TransactionSynchronization.STATUS_COMMITTED) {
            synchronizations.forEach(TransactionSynchronization::afterCommit);
        }
        synchronizations.forEach(s -> s.afterCompletion(status));
    }

    private ProductPhoto photo(long id, String name) {
        ProductPhoto photo = new ProductPhoto();
        photo.setId(id);
        photo.setProduct(product);
        photo.setOriginFileName(name);
        photo.setFileUrl("/uploads/products/UNCATEGORIZED/1_TEST/" + name);
        photo.setPosition(0);
        return photo;
    }

    @Test
    void uploadFile_success_photo() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productPhotoRepository.save(any(ProductPhoto.class))).thenAnswer(invocation -> {
            ProductPhoto saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });
        when(productPhotoRepository.findMaxPositionByProductId(1L)).thenReturn(null);

        ProductPhotoDto result = service.uploadFile(1L, file("image.jpg"));

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getOriginFileName()).isEqualTo("photo_1.jpg");
        assertThat(result.getFileUrl()).isEqualTo("/uploads/products/UNCATEGORIZED/1_TEST/photo_1.jpg");
        assertThat(result.getPosition()).isZero();
        assertThat(productFolder.resolve("photo_1.jpg")).exists();
        verify(productPhotoRepository).save(any(ProductPhoto.class));
        finishTransaction(TransactionSynchronization.STATUS_COMMITTED);
        assertThat(productFolder.resolve("photo_1.jpg")).exists();
    }

    @Test
    void uploadFile_secondPhoto_usesNextNumber() throws IOException {
        Files.createDirectories(productFolder);
        Files.writeString(productFolder.resolve("photo_1.jpg"), "old");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productPhotoRepository.findMaxPositionByProductId(1L)).thenReturn(0);
        when(productPhotoRepository.save(any(ProductPhoto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductPhotoDto result = service.uploadFile(1L, file("new.jpg"));

        assertThat(result.getOriginFileName()).isEqualTo("photo_2.jpg");
        assertThat(result.getPosition()).isEqualTo(1);
        assertThat(Files.readString(productFolder.resolve("photo_1.jpg"))).isEqualTo("old");
    }

    @Test
    void uploadFile_document_success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productPhotoRepository.save(any(ProductPhoto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductPhotoDto result = service.uploadFile(1L, file("invoice.pdf"));

        assertThat(result.getOriginFileName()).isEqualTo("invoice.pdf");
        assertThat(result.getFileUrl()).endsWith("/invoice.pdf");
        assertThat(productFolder.resolve("invoice.pdf")).exists();
    }

    @Test
    void uploadFile_productNotFound_throws() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.uploadFile(1L, file("image.jpg")))
                .isInstanceOf(RestApiException.class)
                .hasMessageContaining("Produkt nicht gefunden");
        verify(productPhotoRepository, never()).save(any(ProductPhoto.class));
    }

    @Test
    void uploadFile_empty_throws() {
        MockMultipartFile empty = new MockMultipartFile("file", "image.jpg", "image/jpeg", new byte[0]);
        assertThatThrownBy(() -> service.uploadFile(1L, empty))
                .isInstanceOf(RestApiException.class)
                .hasMessageContaining("Die Datei ist leer");
        verifyNoInteractions(productRepository);
    }

    @Test
    void uploadFile_unsupportedExtension_throws() {
        assertThatThrownBy(() -> service.uploadFile(1L, file("malware.exe")))
                .isInstanceOf(RestApiException.class)
                .hasMessageContaining("Nicht unterstützter Dateityp");
        verifyNoInteractions(productRepository);
    }

    @Test
    void uploadFile_rollback_removesNewFile() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productPhotoRepository.save(any(ProductPhoto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service.uploadFile(1L, file("image.png"));
        Path path = productFolder.resolve("photo_1.png");
        assertThat(path).exists();
        finishTransaction(TransactionSynchronization.STATUS_ROLLED_BACK);
        assertThat(path).doesNotExist();
    }

    @Test
    void deletePhoto_success_deletesAfterCommit() throws IOException {
        Files.createDirectories(productFolder);
        Path path = productFolder.resolve("photo_1.jpg");
        Files.writeString(path, "old");
        ProductPhoto photo = photo(10L, "photo_1.jpg");
        when(productPhotoRepository.findById(10L)).thenReturn(Optional.of(photo));
        when(productPhotoRepository.findOrderedByProductId(1L)).thenReturn(List.of());

        service.deletePhoto(10L);

        assertThat(path).exists();
        verify(productPhotoRepository).delete(photo);
        verify(productPhotoRepository).flush();
        verify(productPhotoRepository).saveAll(List.of());
        finishTransaction(TransactionSynchronization.STATUS_COMMITTED);
        assertThat(path).doesNotExist();
    }

    @Test
    void deletePhoto_rollback_keepsFile() throws IOException {
        Files.createDirectories(productFolder);
        Path path = productFolder.resolve("photo_1.jpg");
        Files.writeString(path, "old");
        when(productPhotoRepository.findById(10L)).thenReturn(Optional.of(photo(10L, "photo_1.jpg")));
        when(productPhotoRepository.findOrderedByProductId(1L)).thenReturn(List.of());
        service.deletePhoto(10L);
        finishTransaction(TransactionSynchronization.STATUS_ROLLED_BACK);
        assertThat(path).exists();
    }

    @Test
    void deletePhoto_notFound_throws() {
        when(productPhotoRepository.findById(10L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.deletePhoto(10L))
                .isInstanceOf(RestApiException.class)
                .hasMessageContaining("Datei nicht gefunden");
        verify(productPhotoRepository, never()).delete(any(ProductPhoto.class));
    }

    @Test
    void replacePhoto_success_deletesOldAfterCommit() throws IOException {
        Files.createDirectories(productFolder);
        Path oldPath = productFolder.resolve("photo_1.jpg");
        Files.writeString(oldPath, "old");
        ProductPhoto photo = photo(10L, "photo_1.jpg");
        when(productPhotoRepository.findById(10L)).thenReturn(Optional.of(photo));
        when(productPhotoRepository.save(photo)).thenReturn(photo);

        ProductPhotoDto result = service.replacePhoto(10L, file("edited.png"));

        Path newPath = productFolder.resolve("photo_1_edit_1.png");
        assertThat(result.getOriginFileName()).isEqualTo("photo_1_edit_1.png");
        assertThat(oldPath).exists();
        assertThat(newPath).exists();
        finishTransaction(TransactionSynchronization.STATUS_COMMITTED);
        assertThat(oldPath).doesNotExist();
        assertThat(newPath).exists();
    }

    @Test
    void replacePhoto_documentRejected() {
        when(productPhotoRepository.findById(10L)).thenReturn(Optional.of(photo(10L, "invoice.pdf")));
        assertThatThrownBy(() -> service.replacePhoto(10L, file("edited.jpg")))
                .isInstanceOf(RestApiException.class)
                .hasMessageContaining("Nur Fotos können ersetzt werden");
        verify(productPhotoRepository, never()).save(any(ProductPhoto.class));
    }

    @Test
    void reorderPhotos_success() {
        ProductPhoto first = photo(10L, "photo_1.jpg");
        ProductPhoto second = photo(11L, "photo_2.jpg");
        second.setPosition(1);
        when(productPhotoRepository.findOrderedByProductId(1L)).thenReturn(List.of(first, second));

        service.reorderPhotos(1L, List.of(11L, 10L));

        assertThat(first.getPosition()).isEqualTo(1);
        assertThat(second.getPosition()).isZero();
        verify(productPhotoRepository).saveAll(List.of(first, second));
    }

    @Test
    void reorderPhotos_duplicateIds_throws() {
        when(productPhotoRepository.findOrderedByProductId(1L)).thenReturn(List.of(photo(10L, "a.jpg"), photo(11L, "b.jpg")));
        assertThatThrownBy(() -> service.reorderPhotos(1L, List.of(10L, 10L)))
                .isInstanceOf(RestApiException.class)
                .hasMessageContaining("Ungültige Dateireihenfolge");
        verify(productPhotoRepository, never()).saveAll(any());
    }

    @Test
    void getPhotosByProductId_fixesMissingPositions() {
        ProductPhoto first = photo(10L, "photo_1.jpg");
        ProductPhoto second = photo(11L, "photo_2.jpg");
        first.setPosition(null);
        second.setPosition(null);
        when(productPhotoRepository.findOrderedByProductId(1L)).thenReturn(List.of(first, second));

        List<ProductPhotoDto> result = service.getPhotosByProductId(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPosition()).isZero();
        assertThat(result.get(1).getPosition()).isEqualTo(1);
        verify(productPhotoRepository).saveAll(List.of(first, second));
    }

    @Test
    void getAllPhotos_empty_returnsEmptyList() {
        when(productPhotoRepository.findAll()).thenReturn(List.of());
        assertThat(service.getAllPhotos()).isEmpty();
    }
}
