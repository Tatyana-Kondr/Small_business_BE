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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

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
    private static final List<String> DOCUMENT_TYPES = List.of("pdf", "docx", "xlsx", "xls");

    @Override
    public ProductPhoto uploadFile(Long productId, MultipartFile file) {
        try {
            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isEmpty()) {
                throw new RestApiException("Product not found", HttpStatus.NOT_FOUND);
            }

            Product product = productOpt.get();

            String fileExtension = getFileExtension(file.getOriginalFilename());
            Path targetDir;
            if (PHOTO_TYPES.contains(fileExtension.toLowerCase())) {
                targetDir = Paths.get(photoDir);
            } else if (DOCUMENT_TYPES.contains(fileExtension.toLowerCase())) {
                targetDir = Paths.get(documentDir);
            } else {
                throw new RestApiException("Unsupported file type: " + fileExtension, HttpStatus.BAD_REQUEST);
            }

            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            Integer maxPosition = productPhotoRepository.findMaxPositionByProductId(productId);

            int nextPosition = maxPosition == null ? 0 : maxPosition + 1;

            // Создаём временную запись
            ProductPhoto tempPhoto = ProductPhoto.builder()
                    .product(product)
                    .originFileName("temp")
                    .fileUrl("temp")
                    .position(nextPosition)
                    .build();
            tempPhoto = productPhotoRepository.save(tempPhoto);

            // Генерируем уникальное имя файла
            String uniqueFileName = product.getVendorArticle() + "_" + tempPhoto.getId() + "." + fileExtension;

            // Сохраняем файл
            Path filePath = targetDir.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath);

            // Генерируем URL
            String fileUrl = "/uploads/" + (PHOTO_TYPES.contains(fileExtension.toLowerCase()) ? "photos/" : "documents/") + uniqueFileName;

            // Обновляем запись
            tempPhoto.setOriginFileName(uniqueFileName);
            tempPhoto.setFileUrl(fileUrl);
            return productPhotoRepository.save(tempPhoto);

        } catch (IOException e) {
            throw new RestApiException("File upload failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ProductPhoto replacePhoto(Long photoId, MultipartFile file) {

        ProductPhoto photo = productPhotoRepository.findById(photoId)
                .orElseThrow(() -> new RestApiException("Photo not found", HttpStatus.NOT_FOUND));

        try {
            String fileExtension = getFileExtension(file.getOriginalFilename());
            if (fileExtension == null || !PHOTO_TYPES.contains(fileExtension.toLowerCase()))
            {
                throw new RestApiException("Unsupported photo type", HttpStatus.BAD_REQUEST);
            }

            Path targetDir = Paths.get(photoDir);

            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            String newFileName =
                    photo.getProduct().getVendorArticle()
                            + "_"
                            + photo.getId()
                            + "_"
                            + System.currentTimeMillis()
                            + "."
                            + fileExtension;

            Path newFilePath = targetDir.resolve(newFileName);

            // Сначала сохраняем новый файл. Старый пока НЕ удаляем.
            Files.copy(file.getInputStream(), newFilePath);

            String oldFileName = photo.getOriginFileName();

            Path oldFilePath = oldFileName != null ? Paths.get(photoDir).resolve(oldFileName) : null;

            //Меняем только файл. id, product и position остаются теми же
            photo.setOriginFileName(newFileName);
            photo.setFileUrl("/uploads/photos/" + newFileName);

            ProductPhoto saved = productPhotoRepository.save(photo);

            // Только после успешного сохранения записи удаляем старый файл.
            if (oldFilePath != null && !oldFilePath.equals(newFilePath)) {
                Files.deleteIfExists(oldFilePath);
            }
            return saved;

        } catch (IOException e) {
            throw new RestApiException("Photo replacement failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public void reorderPhotos(Long productId, List<Long> photoIds) {

        List<ProductPhoto> photos = productPhotoRepository.findOrderedByProductId(productId);

        if (photos.size() != photoIds.size()) {
            throw new RestApiException("Invalid photo order", HttpStatus.BAD_REQUEST);
        }

        Set<Long> existingIds = photos.stream()
                        .map(ProductPhoto::getId)
                        .collect(Collectors.toSet());

        Set<Long> requestedIds = new HashSet<>(photoIds);

        if (existingIds.size() != requestedIds.size() || !existingIds.equals(requestedIds)) {
            throw new RestApiException("Invalid photo order", HttpStatus.BAD_REQUEST);
        }

        Map<Long, ProductPhoto> photoMap = photos.stream().collect(Collectors.toMap(ProductPhoto::getId, photo -> photo));

        for (int i = 0; i < photoIds.size(); i++) {
            ProductPhoto photo = photoMap.get(photoIds.get(i));
            photo.setPosition(i);
        }

        productPhotoRepository.saveAll(photos);
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

            Long productId = photo.getProduct().getId();

            // Удаляем запись из базы данных
            productPhotoRepository.delete(photo);

            List<ProductPhoto> remainingPhotos = productPhotoRepository.findOrderedByProductId(productId);

            for (int i = 0; i < remainingPhotos.size(); i++) {
                remainingPhotos.get(i).setPosition(i);
            }

            productPhotoRepository.saveAll(remainingPhotos);
        } catch (IOException e) {
            throw new RestApiException("File deletion failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<ProductPhoto> getPhotosByProductId(Long productId) {
        List<ProductPhoto> photos = productPhotoRepository.findOrderedByProductId(productId);

        boolean needsNormalization = photos.stream().anyMatch(photo -> photo.getPosition() == null);

        if (needsNormalization) {
            for (int i = 0; i < photos.size(); i++) {
                photos.get(i).setPosition(i);
            }
            productPhotoRepository.saveAll(photos);
        }

        return photos;
    }

    @Override
    public List<ProductPhoto> getAllPhotos() {
        return productPhotoRepository.findAll();
    }

    private String getFileExtension(String fileName) {
        return StringUtils.getFilenameExtension(fileName);
    }
}