package de.ait.smallBusiness_be.purchases.controllers;

import de.ait.smallBusiness_be.purchases.dto.PurchaseDocumentDto;
import de.ait.smallBusiness_be.purchases.dto.PurchaseScanResultDto;
import de.ait.smallBusiness_be.purchases.model.PurchaseDocument;
import de.ait.smallBusiness_be.purchases.services.PurchaseDocumentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
@Tags(
        @Tag(name = "Purchase Documents controller")
)
public class PurchaseDocumentController {

    private final PurchaseDocumentService purchaseDocumentService;

    @PostMapping(
            value = "/{purchaseId}/documents",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<PurchaseDocumentDto> uploadDocument(
            @PathVariable Long purchaseId,
            @RequestParam("file") MultipartFile file
    ) {
        PurchaseDocumentDto document = purchaseDocumentService.uploadFile(purchaseId, file);
        return ResponseEntity.ok(document);
    }

    @GetMapping("/{purchaseId}/documents")
    public ResponseEntity<List<PurchaseDocumentDto>> getDocuments(@PathVariable Long purchaseId) {
        return ResponseEntity.ok(purchaseDocumentService.getDocuments(purchaseId));
    }

    @DeleteMapping("/documents/{documentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocument(@PathVariable Long documentId) {
        purchaseDocumentService.deleteDocument(documentId);
    }

    @PostMapping(
            value = "/documents/analyze",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<PurchaseScanResultDto> analyzeDocument(
            @RequestParam("file") MultipartFile file
    ) {
        PurchaseScanResultDto result =
                purchaseDocumentService.analyzeDocument(file);

        return ResponseEntity.ok(result);
    }
}
