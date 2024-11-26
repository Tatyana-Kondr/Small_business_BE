package de.ait.smallBusiness_be.products.service.impl;

import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.products.dao.ProductPhotoRepository;
import de.ait.smallBusiness_be.products.dao.ProductRepository;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.products.model.ProductPhoto;
import de.ait.smallBusiness_be.products.service.ProductPhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductPhotoServiceImpl implements ProductPhotoService {

    private final ProductRepository productRepository;
    private final ProductPhotoRepository productPhotoRepository;

    @Value("${file.photo-dir}")
    private String photoDir;

    @Value("${file.document-dir}")
    private String documentDir;

    private static final List<String> PHOTO_TYPES = List.of("jpeg", "jpg", "png");
    private static final List<String> DOCUMENT_TYPES = List.of("pdf", "docx");

    @Override
    public String uploadFile(Long productId, MultipartFile file) {
        try {
            // Проверяем, существует ли продукт
            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isEmpty()) {
                throw new RestApiException("Product not found", HttpStatus.NOT_FOUND);
            }

            Product product = productOpt.get();

            // Проверяем допустимый тип файла
            String fileExtension = getFileExtension(file.getOriginalFilename());
            Path targetDir;
            if (PHOTO_TYPES.contains(fileExtension.toLowerCase())) {
                targetDir = Paths.get(photoDir);
            } else if (DOCUMENT_TYPES.contains(fileExtension.toLowerCase())) {
                targetDir = Paths.get(documentDir);
            } else {
                throw new RestApiException("Unsupported file type: " + fileExtension, HttpStatus.BAD_REQUEST);
            }

            // Создаем директорию, если ее нет
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            // Сохраняем временную запись
            ProductPhoto tempPhoto = ProductPhoto.builder()
                    .product(product)
                    .originFileName("temp")
                    .fileUrl("temp")
                    .build();
            tempPhoto = productPhotoRepository.save(tempPhoto);

            // Генерируем имя файла
            String uniqueFileName = product.getId() + "_" + tempPhoto.getId() + "." + fileExtension;

            // Сохраняем файл
            Path filePath = targetDir.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath);

            // Генерируем URL для файла
            String fileUrl = "/uploads/" + (PHOTO_TYPES.contains(fileExtension.toLowerCase()) ? "photos/" : "documents/") + uniqueFileName;

            // Обновляем запись с финальным именем файла
            tempPhoto.setOriginFileName(uniqueFileName);
            tempPhoto.setFileUrl(fileUrl);
            productPhotoRepository.save(tempPhoto);

            return fileUrl;

        } catch (IOException e) {
            throw new RestApiException("File upload failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deletePhoto(Long photoId) {
        try {
            Optional<ProductPhoto> photoOpt = productPhotoRepository.findById(photoId);
            if (photoOpt.isEmpty()) {
                throw new RestApiException("Photo not found", HttpStatus.NOT_FOUND);
            }

            ProductPhoto photo = photoOpt.get();

            // Определяем директорию файла
            String originFileName = photo.getOriginFileName();
            Path filePath;
            if (PHOTO_TYPES.stream().anyMatch(type -> originFileName.endsWith(type))) {
                filePath = Paths.get(photoDir).resolve(originFileName);
            } else {
                filePath = Paths.get(documentDir).resolve(originFileName);
            }

            // Удаляем файл с диска
            Files.deleteIfExists(filePath);

            // Удаляем запись из базы данных
            productPhotoRepository.delete(photo);
        } catch (IOException e) {
            throw new RestApiException("File deletion failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<ProductPhoto> getPhotosByProductId(Long productId) {
        return productPhotoRepository.findByProductId(productId);
    }

    private String getFileExtension(String fileName) {
        return StringUtils.getFilenameExtension(fileName);
    }
}