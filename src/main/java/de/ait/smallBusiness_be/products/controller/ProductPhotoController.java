package de.ait.smallBusiness_be.products.controller;


import de.ait.smallBusiness_be.products.dto.ProductPhotoOrderDto;
import de.ait.smallBusiness_be.products.model.ProductPhoto;
import de.ait.smallBusiness_be.products.service.ProductPhotoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tags(
        @Tag(name = "Product Photos controller")
)
public class ProductPhotoController {

    private final ProductPhotoService productPhotoService;


    @PostMapping(value = "/{productId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductPhoto> uploadFile(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file) {
        try {
            ProductPhoto savedPhoto = productPhotoService.uploadFile(productId, file);
            return ResponseEntity.ok(savedPhoto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping(value = "/photos/{photoId}/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductPhoto> replacePhoto(
            @PathVariable Long photoId,
            @RequestParam("file") MultipartFile file) {
        ProductPhoto photo = productPhotoService.replacePhoto(photoId, file);
        return ResponseEntity.ok(photo);
    }

    @PutMapping("/{productId}/photos/order")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorderPhotos(
            @PathVariable Long productId,
            @RequestBody ProductPhotoOrderDto dto) {
        productPhotoService.reorderPhotos(productId, dto.getPhotoIds());
    }

    @DeleteMapping("/photos/{photoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePhoto(@PathVariable Long photoId) {
        productPhotoService.deletePhoto(photoId);
    }


    @GetMapping("/{productId}/photos")
    public ResponseEntity<List<ProductPhoto>> getPhotos(@PathVariable Long productId) {
        List<ProductPhoto> photos = productPhotoService.getPhotosByProductId(productId);
        return ResponseEntity.ok(photos);
    }

    @GetMapping("/photos")
    public ResponseEntity<List<ProductPhoto>> getAllProductPhotos() {
        List<ProductPhoto> photos = productPhotoService.getAllPhotos();
        return ResponseEntity.ok(photos);
    }
}