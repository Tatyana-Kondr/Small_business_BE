package de.ait.smallBusiness_be.products.controller;


import de.ait.smallBusiness_be.products.model.ProductPhoto;
import de.ait.smallBusiness_be.products.service.ProductPhotoService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductPhotoController {

    private final ProductPhotoService productPhotoService;


    @PostMapping(value = "/{productId}/files", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<String> uploadFile(
            @PathVariable Long productId,
            @Parameter(description = "Product photo file", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE))
            @RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = productPhotoService.uploadFile(productId, file);
            return ResponseEntity.ok("File uploaded successfully: " + fileUrl);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(500).body("File upload failed");
        }
    }

    @DeleteMapping("/photos/{photoId}")
    public ResponseEntity<String> deletePhoto(@PathVariable Long photoId) {
        try {
            productPhotoService.deletePhoto(photoId);
            return ResponseEntity.ok("Photo deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(500).body("File deletion failed");
        }
    }

    @GetMapping("/{productId}/photos")
    public ResponseEntity<List<ProductPhoto>> getPhotos(@PathVariable Long productId) {
        List<ProductPhoto> photos = productPhotoService.getPhotosByProductId(productId);
        return ResponseEntity.ok(photos);
    }
}