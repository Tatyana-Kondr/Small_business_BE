package de.ait.smallBusiness_be.products.service.impl;

import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.FieldValidationException;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.productions.dao.ProductionItemRepository;
import de.ait.smallBusiness_be.productions.dao.ProductionRepository;
import de.ait.smallBusiness_be.products.dao.ProductPhotoRepository;
import de.ait.smallBusiness_be.products.dao.ProductRepository;
import de.ait.smallBusiness_be.products.dto.*;
import de.ait.smallBusiness_be.products.model.*;
import de.ait.smallBusiness_be.products.service.ProductCategoryService;
import de.ait.smallBusiness_be.products.service.ProductService;
import de.ait.smallBusiness_be.products.service.UnitOfMeasurementService;
import de.ait.smallBusiness_be.purchases.dao.PurchaseItemRepository;
import de.ait.smallBusiness_be.sales.dao.SaleItemRepository;
import de.ait.smallBusiness_be.validation.dto.ValidationErrorDto;
import de.ait.smallBusiness_be.warehouse.dao.WarehouseRecordRepository;
import de.ait.smallBusiness_be.warehouse.dao.WarehouseRepository;
import de.ait.smallBusiness_be.warehouse.models.Warehouse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * 11/15/2024
 * Small_business_BE
 *
 * @author Chechkina (AIT TR)
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryService productCategoryService;
    private final UnitOfMeasurementService unitOfMeasurementService;
    private final ProductPhotoRepository productPhotoRepository;
    private final WarehouseRepository warehouseRepository;
    private final SaleItemRepository saleItemRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final ProductionRepository productionRepository;
    private final ProductionItemRepository productionItemRepository;
    private final WarehouseRecordRepository warehouseRecordRepository;

     private final ModelMapper modelMapper;

    @Value("${file.photo-dir}")
    private String photoDir;

    @Value("${file.document-dir}")
    private String documentDir;

    @Override
    @Transactional
    public ProductDto addProduct(NewProductDto newProductDto) {

        ProductCategory category = productCategoryService.getProductCategoryOrThrow(newProductDto.getProductCategoryId());
        UnitOfMeasurement unit = unitOfMeasurementService.getUnitOfMeasurementOrThrow(newProductDto.getUnitOfMeasurementId());

        Product product = modelMapper.map(newProductDto, Product.class);

        product.setProductCategory(category);
        product.setUnitOfMeasurement(unit);

        Product savedProduct = productRepository.save(product);

        String article = category.getArtName() + "-" + savedProduct.getId();
        savedProduct.setArticle(article);

        savedProduct = productRepository.save(savedProduct);

        return modelMapper.map(savedProduct, ProductDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductById(Long id) {
        Product product = getProductOrThrow(id);
        return mapToProductDto(product);
    }

    private Path resolveProductFilePath(ProductPhoto file) {

        String fileName = file.getOriginFileName();
        String fileUrl = file.getFileUrl();

        if (fileName == null || fileName.isBlank()
                || fileUrl == null || fileUrl.isBlank()) {
            throw new IllegalStateException(
                    "Ungültige Produktdatei: " + file.getId()
            );
        }

        Path baseDir;

        if (fileUrl.startsWith("/uploads/photos/")) {
            baseDir = Paths.get(photoDir);
        } else if (fileUrl.startsWith("/uploads/documents/")) {
            baseDir = Paths.get(documentDir);
        } else {
            throw new IllegalStateException(
                    "Unbekannter Dateipfad: " + fileUrl
            );
        }

        Path normalizedBase = baseDir.toAbsolutePath().normalize();

        Path resolvedPath = normalizedBase.resolve(fileName).normalize();

        if (!resolvedPath.startsWith(normalizedBase)
                || resolvedPath.equals(normalizedBase)) {
            throw new IllegalStateException(
                    "Ungültiger Dateiname: " + fileName
            );
        }

        return resolvedPath;
    }


    @Override
    @Transactional
    public void deleteProductById(Long id) {
        Product product = getProductOrThrow(id);

        // 1. Проверяем, используется ли продукт в документах
        List<String> usedIn = new ArrayList<>();
        if (saleItemRepository.existsByProduct_Id(id)) {
            usedIn.add("Verkäufen");
        }
        if (purchaseItemRepository.existsByProduct_Id(id)) {
            usedIn.add("Einkäufen");
        }
        if (productionRepository.existsByProduct_Id(id)) {
            usedIn.add("Produktionen");
        }
        if (productionItemRepository.existsByProduct_Id(id)) {
            usedIn.add("Produktionsmaterialien");
        }
        if (warehouseRecordRepository.existsByProduct_Id(id)) {
            usedIn.add("Lagerbewegungen");
        }

        if (!usedIn.isEmpty()) {
            String message =
                    "Das Produkt kann nicht gelöscht werden, da es bereits in folgenden Vorgängen verwendet wird: " + String.join(", ", usedIn) + ".";

            throw new FieldValidationException(
                    List.of(ValidationErrorDto.builder().field("product").message(message).build()));
        }

        // 2. Проверяем складской остаток
        Warehouse warehouse = warehouseRepository.findByProduct(product).orElse(null);

        if (warehouse != null && warehouse.getQuantity() != null && warehouse.getQuantity().compareTo(BigDecimal.ZERO) != 0) {

            throw new FieldValidationException(
                    List.of(ValidationErrorDto.builder()
                                    .field("product")
                                    .message("Das Produkt kann nicht gelöscht werden, da noch ein Lagerbestand vorhanden ist.").build()));
        }

        // 3. Получаем фотографии, которые потребуется удалить
        List<ProductPhoto> photos = productPhotoRepository.findOrderedByProductId(id);

        List<Path> filePaths = photos.stream()
                .map(this::resolveProductFilePath)
                .distinct()
                .toList();

        // 4. Удаляем связанные записи и сам продукт
        try {
            if (!photos.isEmpty()) {
                productPhotoRepository.deleteAll(photos);
                productPhotoRepository.flush();
            }

            if (warehouse != null) {
                warehouseRepository.delete(warehouse);
                warehouseRepository.flush();
            }

            productRepository.delete(product);
            productRepository.flush();

        } catch (DataIntegrityViolationException e) {
            throw new RestApiException(ErrorDescription.PRODUCT_DELETE_FAILED, HttpStatus.CONFLICT);
        }

        // 5. Файлы удаляем только после успешного COMMIT
        if (!filePaths.isEmpty()) {

            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {

                        @Override
                        public void afterCommit() {
                            for (Path filePath : filePaths) {
                                try {
                                    Files.deleteIfExists(filePath);
                                    log.info("Produktdatei gelöscht: {}", filePath);

                                } catch (IOException | SecurityException e) {
                                    log.error("Fehler beim Löschen der Produktdatei: {}", filePath, e);
                                }
                            }
                        }
                    }
            );
        }
    }



    @Override
    @Transactional
    public ProductDto updateProduct(Long id, UpdateProductDto updateProductDto) {

        Product product = getProductOrThrow(id);
        ProductCategory category = productCategoryService.getProductCategoryOrThrow(updateProductDto.getProductCategoryId());
        UnitOfMeasurement unit = unitOfMeasurementService.getUnitOfMeasurementOrThrow(updateProductDto.getUnitOfMeasurementId());

        product.setName(updateProductDto.getName());
        product.setArticle(updateProductDto.getArticle());
        product.setVendorArticle(updateProductDto.getVendorArticle());
        product.setPurchasingPrice(updateProductDto.getPurchasingPrice());
        product.setMarkupPercentage(updateProductDto.getMarkupPercentage());
        product.setSellingPrice(updateProductDto.getSellingPrice());

        product.setUnitOfMeasurement(unit);
        product.setWeight(updateProductDto.getWeight());

        if (updateProductDto.getNewDimensions() != null) {
            Dimensions dimensions = modelMapper.map(updateProductDto.getNewDimensions(), Dimensions.class);
            product.setDimensions(dimensions);
        } else {
            product.setDimensions(null);
        }

        product.setProductCategory(category);
        product.setDescription(updateProductDto.getDescription());
        product.setCustomsNumber(updateProductDto.getCustomsNumber());
        product.setStorageLocation(updateProductDto.getStorageLocation());

        Product updatedProduct = productRepository.save(product);

        return mapToProductDto(updatedProduct);
    }


    @Override
    @Transactional
    public ProductDto updateProductArticle(Long id, UpdateProductArticleDto dto) {
        Product product = getProductOrThrow(id);
        product.setArticle(dto.getArticle().trim());
        return mapToProductDto(product);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> findProducts(String searchTerm, Pageable pageable) {
        Page<Product> productsPage =
                StringUtils.hasText(searchTerm)
                        ? productRepository.searchProductsPage(searchTerm, pageable)
                        : productRepository.findAll(pageable);

        return mapToProductDtoPage(productsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> findProductsByCategoryId(Long categoryId, String searchTerm, Pageable pageable) {
        Page<Product> productsPage =
                StringUtils.hasText(searchTerm)
                        ? productRepository.searchProductsByCategoryPage(categoryId, searchTerm, pageable)
                        : productRepository.findByProductCategory_Id(categoryId, pageable);

        return mapToProductDtoPage(productsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductPickDto> pick(String searchTerm, Long categoryId, Integer limit) {
        String q = (searchTerm == null) ? "" : searchTerm.trim();
        int safeLimit = (limit == null) ? 50 : Math.min(Math.max(limit, 10), 100);

        // Если нет категории и строка меньше 2 символов — не ищем
        if (categoryId == null && q.length() < 2) {
            return List.of();
        }

        return productRepository.pickProducts(q, categoryId, PageRequest.of(0, safeLimit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductPickDto> findSimilarProducts(String name, String vendorArticle) {
        if (!StringUtils.hasText(name)) {
            return List.of();
        }
        String normalizedName = name.trim();
        String normalizedVendorArticle = StringUtils.hasText(vendorArticle) ? vendorArticle.trim() : null;

        return productRepository.findSimilarProducts(normalizedName, normalizedVendorArticle);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductPickDto> findSimilarProductsForUpdate(Long productId, String name, String article, String vendorArticle) {
        getProductOrThrow(productId);
        return productRepository.findSimilarProductsForUpdate(productId, normalizeNullable(name), normalizeNullable(article), normalizeNullable(vendorArticle));
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    public Product getProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RestApiException(ErrorDescription.PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private Page<ProductDto> mapToProductDtoPage(Page<Product> productsPage) {

        if (productsPage.isEmpty()) {
            return productsPage.map(product -> modelMapper.map(product, ProductDto.class));
        }

        List<Long> productIds = productsPage
                .getContent()
                .stream()
                .map(Product::getId)
                .toList();

        Set<Long> productIdsWithPhotos = new HashSet<>(
                productPhotoRepository.findProductIdsWithPhotos(productIds)
        );

        Map<Long, BigDecimal> quantityByProductId =
                warehouseRepository.findAllByProduct_IdIn(productIds)
                        .stream()
                        .collect(Collectors.toMap(
                                warehouse -> warehouse.getProduct().getId(), Warehouse::getQuantity));

        return productsPage.map(product -> {
            ProductDto productDto = modelMapper.map(product, ProductDto.class);

            if (product.getDimensions() != null) {
                NewDimensionsDto dimensionsDto = modelMapper.map(product.getDimensions(), NewDimensionsDto.class);

                productDto.setNewDimensions(dimensionsDto);
            }

            productDto.setHasPhoto(productIdsWithPhotos.contains(product.getId()));
            productDto.setQuantity(quantityByProductId.getOrDefault(product.getId(), BigDecimal.ZERO));

            return productDto;
        });
    }

    private ProductDto mapToProductDto(Product product) {
        ProductDto productDto = modelMapper.map(product, ProductDto.class);

        if (product.getDimensions() != null) {
            NewDimensionsDto dimensionsDto =
                    modelMapper.map(
                            product.getDimensions(),
                            NewDimensionsDto.class
                    );

            productDto.setNewDimensions(dimensionsDto);
        }

        boolean hasPhoto = !productPhotoRepository
                .findOrderedByProductId(product.getId())
                .isEmpty();

        productDto.setHasPhoto(hasPhoto);

        BigDecimal quantity = warehouseRepository
                .findByProduct(product)
                .map(Warehouse::getQuantity)
                .orElse(BigDecimal.ZERO);

        productDto.setQuantity(quantity);

        return productDto;
    }
}
