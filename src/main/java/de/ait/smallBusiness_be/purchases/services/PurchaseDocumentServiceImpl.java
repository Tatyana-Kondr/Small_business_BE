package de.ait.smallBusiness_be.purchases.services;

import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.purchases.dao.PurchaseDocumentRepository;
import de.ait.smallBusiness_be.purchases.dao.PurchaseRepository;
import de.ait.smallBusiness_be.purchases.dto.PurchaseDocumentDto;
import de.ait.smallBusiness_be.purchases.model.Purchase;
import de.ait.smallBusiness_be.purchases.model.PurchaseDocument;
import jakarta.persistence.EntityNotFoundException;
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

        Purchase purchase = getPurchaseOrThrow(purchaseId);

        if (file == null || file.isEmpty()) {
            throw new RestApiException(ErrorDescription.FILE_IS_EMPTY, HttpStatus.BAD_REQUEST);
        }

        String originalFileName = file.getOriginalFilename();

        if (!StringUtils.hasText(originalFileName)) {
            throw new RestApiException(ErrorDescription.FILE_NAME_IS_EMPTY, HttpStatus.BAD_REQUEST);
        }

        String safeOriginalFileName = Paths.get(originalFileName).getFileName().toString();

        String fileExtension = StringUtils.getFilenameExtension(safeOriginalFileName);

        if (fileExtension == null || !ALLOWED_TYPES.contains(fileExtension.toLowerCase())) {
            throw new RestApiException(ErrorDescription.UNSUPPORTED_FILE_TYPE, HttpStatus.BAD_REQUEST);
        }

        try {
            Path targetDir = Paths.get(documentDir);
            Files.createDirectories(targetDir);

            String extension = fileExtension.toLowerCase();

            /* Например: 15_2026-09-23_Amazon_RECHNUNG_RE12345678  */
            String baseFileName = buildBaseFileName(purchase);
            /*
             * Если файла ещё нет:
             * 15_2026-09-23_Amazon_RECHNUNG_RE12345678.pdf
             * Если уже существует:
             * 15_2026-09-23_Amazon_RECHNUNG_RE12345678_2.pdf
             * 15_2026-09-23_Amazon_RECHNUNG_RE12345678_3.pdf
             */

            String storedFileName = buildUniqueFileName(targetDir, baseFileName, extension);

            Path filePath = targetDir.resolve(storedFileName);

            Files.copy(file.getInputStream(), filePath);

            String fileUrl = "/uploads/documents/" + storedFileName;

            PurchaseDocument document =
                    PurchaseDocument.builder()
                            .purchase(purchase)
                            .originFileName(safeOriginalFileName)
                            .fileUrl(fileUrl)
                            .contentType(
                                    StringUtils.hasText(file.getContentType())
                                            ? file.getContentType()
                                            : "application/octet-stream"
                            )
                            .createdDate(LocalDateTime.now())
                            .build();

            PurchaseDocument savedDocument = purchaseDocumentRepository.save(document);

            return mapToDto(savedDocument);

        } catch (IOException e) {
            throw new RestApiException(ErrorDescription.FILE_UPLOAD_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<PurchaseDocumentDto> getDocuments(Long purchaseId) {

        getPurchaseOrThrow(purchaseId);

        return purchaseDocumentRepository
                .findAllByPurchaseIdOrderByIdAsc(purchaseId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public void deleteDocument(Long documentId) {

        PurchaseDocument document = purchaseDocumentRepository.findById(documentId)
                        .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.FILE_NOT_FOUND.getDescription()));

        try {
            String fileName = Paths.get(document.getFileUrl()).getFileName().toString();
            Path filePath = Paths.get(documentDir).resolve(fileName);
            Files.deleteIfExists(filePath);

            purchaseDocumentRepository.delete(document);

        } catch (IOException e) {
            throw new RestApiException(ErrorDescription.FILE_DELETE_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String buildBaseFileName(Purchase purchase) {

        StringBuilder fileName = new StringBuilder();

        fileName
                .append(purchase.getId())
                .append("_")
                .append(purchase.getPurchasingDate())
                .append("_")
                .append(sanitizeFilePart(purchase.getVendor().getName()))
                .append("_")
                .append(sanitizeFilePart(purchase.getDocument().getName()));

        if (StringUtils.hasText(purchase.getDocumentNumber())) {
            fileName.append("_").append(sanitizeFilePart(purchase.getDocumentNumber()));
        }

        return fileName.toString();
    }

    /**
     * Если файл с таким именем уже существует,
     * добавляет _2, _3, _4 и т.д.
     */
    private String buildUniqueFileName(Path targetDir, String baseFileName, String extension) {

        String fileName = baseFileName + "." + extension;
        Path filePath = targetDir.resolve(fileName);

        if (!Files.exists(filePath)) {
            return fileName;
        }

        int counter = 2;

        while (true) {
            fileName = baseFileName + "_" + counter + "." + extension;
            filePath = targetDir.resolve(fileName);

            if (!Files.exists(filePath)) {
                return fileName;
            }

            counter++;
        }
    }

    private String sanitizeFilePart(String value) {
        if (!StringUtils.hasText(value)) {return "";}

        return value
                .trim()
                .replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll("\\s+", "_");
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

    private Purchase getPurchaseOrThrow(Long purchaseId) {
        return purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.PURCHASE_NOT_FOUND.getDescription()));
    }
}
