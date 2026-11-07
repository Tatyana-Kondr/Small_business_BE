package de.ait.smallBusiness_be.products.service.impl;

import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.products.dao.ProductPhotoRepository;
import de.ait.smallBusiness_be.products.dao.ProductRepository;
import de.ait.smallBusiness_be.products.dto.ProductPhotoDto;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.products.model.ProductPhoto;
import de.ait.smallBusiness_be.products.service.ProductPhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductPhotoServiceImpl implements ProductPhotoService {

    private final ProductRepository productRepository;
    private final ProductPhotoRepository productPhotoRepository;

    @Value("${app.uploads.root}")
    private String uploadRoot;

    @Value("${app.uploads.products}")
    private String productsDir;

    private static final Set<String> PHOTO_TYPES = Set.of("jpg", "jpeg", "png");
    private static final Set<String> DOCUMENT_TYPES = Set.of("pdf", "doc", "docx", "xlsx", "xls");

    @Override
    @Transactional
    public ProductPhotoDto uploadFile(Long productId, MultipartFile file) {
        String extension = checkedExtension(file, false);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RestApiException("Produkt nicht gefunden.", HttpStatus.NOT_FOUND));
        Path folder = productFolder(product);
        try {
            Files.createDirectories(folder);
            String fileName;
            Path target;
            // CREATE_NEW prevents overwriting an existing file even under concurrent requests.
            if (PHOTO_TYPES.contains(extension)) {
                int number = 1;
                while (true) {
                    fileName = "photo_" + number + "." + extension;
                    target = folder.resolve(fileName);
                    try {
                        writeNewFile(file, target);
                        break;
                    } catch (FileAlreadyExistsException e) {
                        number++;
                    }
                }
            } else {
                String original = safeFileName(file.getOriginalFilename());
                String stem = original.substring(0, original.lastIndexOf('.'));
                int number = 1;
                while (true) {
                    fileName = (number == 1 ? stem : stem + "_" + number) + "." + extension;
                    target = folder.resolve(fileName);
                    try {
                        writeNewFile(file, target);
                        break;
                    } catch (FileAlreadyExistsException e) {
                        number++;
                    }
                }
            }
            final Path createdFile = target;
            onRollback(() -> deleteQuietly(createdFile));

            Integer maxPosition = productPhotoRepository.findMaxPositionByProductId(productId);
            ProductPhoto photo = ProductPhoto.builder()
                    .product(product)
                    .originFileName(fileName)
                    .fileUrl(toFileUrl(createdFile))
                    .position(maxPosition == null ? 0 : maxPosition + 1)
                    .build();
            return mapToDto(productPhotoRepository.save(photo));
        } catch (IOException e) {
            throw new RestApiException("Datei konnte nicht hochgeladen werden: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public ProductPhotoDto replacePhoto(Long photoId, MultipartFile file) {
        String extension = checkedExtension(file, true);
        ProductPhoto photo = productPhotoRepository.findById(photoId)
                .orElseThrow(() -> new RestApiException("Foto nicht gefunden.", HttpStatus.NOT_FOUND));
        if (!isPhotoUrl(photo.getFileUrl())) {
            throw new RestApiException("Nur Fotos können ersetzt werden.", HttpStatus.BAD_REQUEST);
        }
        Path oldPath = resolveFileUrl(photo.getFileUrl());
        Path folder = oldPath.getParent(); // Old uploads remain in their existing location.
        try {
            Files.createDirectories(folder);
            Path newPath;
            String newName;
            // Do not overwrite the old photo; retain its number when possible.
            String oldName = oldPath.getFileName().toString();
            String base = oldName.replaceFirst("(?i)\\.[^.]+$", "");
            int version = 1;
            while (true) {
                newName = base + "_edit_" + version + "." + extension;
                newPath = folder.resolve(newName);
                try {
                    writeNewFile(file, newPath);
                    break;
                } catch (FileAlreadyExistsException e) {
                    version++;
                }
            }
            final Path created = newPath;
            onRollback(() -> deleteQuietly(created));
            photo.setOriginFileName(newName);
            photo.setFileUrl(toFileUrl(created));
            ProductPhoto saved = productPhotoRepository.save(photo);
            onCommit(() -> deleteQuietly(oldPath));
            return mapToDto(saved);
        } catch (IOException e) {
            throw new RestApiException("Foto konnte nicht ersetzt werden: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public void deletePhoto(Long photoId) {
        ProductPhoto photo = productPhotoRepository.findById(photoId)
                .orElseThrow(() -> new RestApiException("Datei nicht gefunden.", HttpStatus.NOT_FOUND));
        Path oldPath = resolveFileUrl(photo.getFileUrl());
        Long productId = photo.getProduct().getId();
        productPhotoRepository.delete(photo);
        productPhotoRepository.flush();
        List<ProductPhoto> remaining = productPhotoRepository.findOrderedByProductId(productId);
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setPosition(i);
        }
        productPhotoRepository.saveAll(remaining);
        onCommit(() -> deleteQuietly(oldPath));
    }

    @Override
    @Transactional
    public void reorderPhotos(Long productId, List<Long> photoIds) {
        if (photoIds == null) {
            throw new RestApiException("Ungültige Dateireihenfolge.", HttpStatus.BAD_REQUEST);
        }
        List<ProductPhoto> photos = productPhotoRepository.findOrderedByProductId(productId);
        Set<Long> existing = photos.stream().map(ProductPhoto::getId).collect(Collectors.toSet());
        Set<Long> requested = new HashSet<>(photoIds);
        if (photos.size() != photoIds.size() || requested.size() != photoIds.size()
                || !existing.equals(requested)) {
            throw new RestApiException("Ungültige Dateireihenfolge.", HttpStatus.BAD_REQUEST);
        }
        Map<Long, ProductPhoto> byId = photos.stream()
                .collect(Collectors.toMap(ProductPhoto::getId, p -> p));
        for (int i = 0; i < photoIds.size(); i++) {
            byId.get(photoIds.get(i)).setPosition(i);
        }
        productPhotoRepository.saveAll(photos);
    }

    @Override
    @Transactional
    public List<ProductPhotoDto> getPhotosByProductId(Long productId) {
        List<ProductPhoto> photos = productPhotoRepository.findOrderedByProductId(productId);
        if (photos.stream().anyMatch(p -> p.getPosition() == null)) {
            for (int i = 0; i < photos.size(); i++) photos.get(i).setPosition(i);
            productPhotoRepository.saveAll(photos);
        }
        return photos.stream().map(this::mapToDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductPhotoDto> getAllPhotos() {
        return productPhotoRepository.findAll().stream().map(this::mapToDto).toList();
    }

    private Path productFolder(Product product) {
        String category = product.getProductCategory() == null ? "UNCATEGORIZED"
                : safeSegment(product.getProductCategory().getArtName(), "UNCATEGORIZED");
        String vendor = safeSegment(product.getVendorArticle(), "");
        String folderName = product.getId() + (vendor.isEmpty() ? "" : "_" + vendor);
        Path root = Paths.get(productsDir).toAbsolutePath().normalize();
        Path result = root.resolve(category).resolve(folderName).normalize();
        if (!result.startsWith(root)) throw new IllegalStateException("Ungültiger Produktpfad");
        return result;
    }

    private String checkedExtension(MultipartFile file, boolean photoOnly) {
        if (file == null || file.isEmpty()) {
            throw new RestApiException("Die Datei ist leer.", HttpStatus.BAD_REQUEST);
        }
        String name = file.getOriginalFilename();
        int dot = name == null ? -1 : name.lastIndexOf('.');
        if (dot <= 0 || dot == name.length() - 1) {
            throw new RestApiException("Die Datei hat keine gültige Dateiendung.", HttpStatus.BAD_REQUEST);
        }
        String ext = name.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!(PHOTO_TYPES.contains(ext) || (!photoOnly && DOCUMENT_TYPES.contains(ext)))) {
            throw new RestApiException("Nicht unterstützter Dateityp: " + ext, HttpStatus.BAD_REQUEST);
        }
        return ext;
    }

    private String safeSegment(String value, String fallback) {
        if (value == null || value.isBlank()) return fallback;
        String safe = value.trim().replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_")
                .replaceAll("\\s+", "_").replaceAll("\\.+$", "");
        if (safe.equals(".") || safe.equals("..") || safe.isBlank()) return fallback;
        return safe.length() > 90 ? safe.substring(0, 90) : safe;
    }

    private String safeFileName(String original) {
        String name = original == null ? "document" : original.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1);
        int dot = name.lastIndexOf('.');
        String stem = dot > 0 ? name.substring(0, dot) : "document";
        String ext = dot > 0 ? name.substring(dot + 1).toLowerCase(Locale.ROOT) : "pdf";
        return safeSegment(stem, "document") + "." + ext;
    }

    private void writeNewFile(MultipartFile file, Path target) throws IOException {
        // CREATE_NEW prevents accidental replacement of existing files.
        try (InputStream in = file.getInputStream()) {
            try (var out = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                in.transferTo(out);
            } catch (IOException e) {
                // Do not delete on FileAlreadyExistsException: that file belongs to someone else.
                if (!(e instanceof FileAlreadyExistsException)) deleteQuietly(target);
                throw e;
            }
        }
    }

    private Path uploadsRootPath() {
        return Paths.get(uploadRoot).toAbsolutePath().normalize();
    }

    private String toFileUrl(Path file) {
        Path root = uploadsRootPath();
        Path absolute = file.toAbsolutePath().normalize();
        if (!absolute.startsWith(root)) {
            throw new IllegalStateException("Datei liegt außerhalb von uploads: " + absolute);
        }
        StringJoiner joiner = new StringJoiner("/", "/uploads/", "");
        for (Path part : root.relativize(absolute)) {
            joiner.add(UriUtils.encodePathSegment(part.toString(), java.nio.charset.StandardCharsets.UTF_8));
        }
        return joiner.toString();
    }

    private Path resolveFileUrl(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith("/uploads/")) {
            throw new RestApiException("Ungültiger Dateipfad.", HttpStatus.BAD_REQUEST);
        }
        String relative = fileUrl.substring("/uploads/".length());
        // URLs produced by this service may have encoded spaces/non-ASCII characters.
        String decoded = UriUtils.decode(relative, java.nio.charset.StandardCharsets.UTF_8);
        Path root = uploadsRootPath();
        Path path = root.resolve(decoded).normalize();
        if (!path.startsWith(root)) {
            throw new RestApiException("Ungültiger Dateipfad.", HttpStatus.BAD_REQUEST);
        }
        return path;
    }

    private boolean isPhotoUrl(String url) {
        if (url == null) return false;
        String path = url.toLowerCase(Locale.ROOT);
        return PHOTO_TYPES.stream().anyMatch(ext -> path.endsWith("." + ext));
    }

    private void onCommit(Runnable action) {
        register(action, null);
    }

    private void onRollback(Runnable action) {
        register(null, action);
    }

    private void register(Runnable committed, Runnable rolledBack) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("Eine aktive Transaktion ist erforderlich.");
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                if (committed != null) committed.run();
            }
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED && rolledBack != null) rolledBack.run();
            }
        });
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("Datei konnte nicht gelöscht werden: {}", path, e);
        }
    }

    private ProductPhotoDto mapToDto(ProductPhoto photo) {
        return new ProductPhotoDto(photo.getId(), photo.getOriginFileName(),
                photo.getFileUrl(), photo.getPosition());
    }
}
