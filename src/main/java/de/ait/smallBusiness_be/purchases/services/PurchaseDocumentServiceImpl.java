package de.ait.smallBusiness_be.purchases.services;

import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.purchases.dao.PurchaseDocumentRepository;
import de.ait.smallBusiness_be.purchases.dao.PurchaseRepository;
import de.ait.smallBusiness_be.purchases.dto.PurchaseDocumentDto;
import de.ait.smallBusiness_be.purchases.dto.PurchaseScanResultDto;
import de.ait.smallBusiness_be.purchases.model.Purchase;
import de.ait.smallBusiness_be.purchases.model.PurchaseDocument;
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
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseDocumentServiceImpl implements PurchaseDocumentService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseDocumentRepository purchaseDocumentRepository;

    @Value("${file.document-dir}")
    private String documentDir;

    private static final List<String> ALLOWED_TYPES =
            List.of("jpeg", "jpg", "png", "pdf");

    @Override
    public PurchaseDocumentDto uploadFile(Long purchaseId, MultipartFile file) {

        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() ->
                        new RestApiException(
                                "Purchase not found",
                                HttpStatus.NOT_FOUND
                        )
                );

        String originalFileName = file.getOriginalFilename();

        if (!StringUtils.hasText(originalFileName)) {
            throw new RestApiException(
                    "File name is empty",
                    HttpStatus.BAD_REQUEST
            );
        }

        String fileExtension = StringUtils.getFilenameExtension(originalFileName);

        if (fileExtension == null || !ALLOWED_TYPES.contains(fileExtension.toLowerCase())) {

            throw new RestApiException(
                    "Unsupported file type",
                    HttpStatus.BAD_REQUEST
            );
        }

        try {
            Path targetDir = Paths.get(documentDir);

            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            String safeFileName =
                    Paths.get(originalFileName)
                            .getFileName()
                            .toString();

            Path filePath = targetDir.resolve(safeFileName);

            Files.copy(
                    file.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            String fileUrl =
                    "/uploads/documents/" + safeFileName;

            PurchaseDocument document =
                    PurchaseDocument.builder()
                            .purchase(purchase)
                            .originFileName(safeFileName)
                            .fileUrl(fileUrl)
                            .contentType(
                                    file.getContentType() != null
                                            ? file.getContentType()
                                            : "application/octet-stream"
                            )
                            .createdDate(LocalDateTime.now())
                            .build();

            PurchaseDocument savedDocument =
                    purchaseDocumentRepository.save(document);

            return mapToDto(savedDocument);

        } catch (IOException e) {

            throw new RestApiException(
                    "Document upload failed: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public List<PurchaseDocumentDto> getDocuments(Long purchaseId) {

        if (!purchaseRepository.existsById(purchaseId)) {
            throw new RestApiException(
                    "Purchase not found",
                    HttpStatus.NOT_FOUND
            );
        }

        return purchaseDocumentRepository
                .findAllByPurchaseIdOrderByIdAsc(purchaseId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public void deleteDocument(Long documentId) {

        PurchaseDocument document =
                purchaseDocumentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new RestApiException(
                                        "Document not found",
                                        HttpStatus.NOT_FOUND
                                )
                        );

        try {
            String fileName =
                    Paths.get(document.getFileUrl())
                            .getFileName()
                            .toString();

            Path filePath =
                    Paths.get(documentDir)
                            .resolve(fileName);

            Files.deleteIfExists(filePath);

            purchaseDocumentRepository.delete(document);

        } catch (IOException e) {

            throw new RestApiException(
                    "Document deletion failed: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public PurchaseScanResultDto analyzeDocument(MultipartFile file) {

        return PurchaseScanResultDto.builder()
                .vendorName("Firma Müller GmbH")
                .document("RECHNUNG")
                .documentNumber("RE-2026-1845")
                .purchasingDate(LocalDate.of(2026, 9, 8))
                .build();
    }

    private PurchaseDocumentDto mapToDto(PurchaseDocument document) {
        return PurchaseDocumentDto.builder()
                .id(document.getId())
                .purchaseId(document.getPurchase().getId())
                .originFileName(document.getOriginFileName())
                .fileUrl(document.getFileUrl())
                .contentType(document.getContentType())
                .createdDate(document.getCreatedDate())
                .build();
    }
}
