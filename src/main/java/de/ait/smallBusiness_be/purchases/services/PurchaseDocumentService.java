package de.ait.smallBusiness_be.purchases.services;

import de.ait.smallBusiness_be.purchases.dto.PurchaseDocumentDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PurchaseDocumentService {

    PurchaseDocumentDto uploadFile(Long purchaseId, MultipartFile file);

    List<PurchaseDocumentDto> getDocuments(Long purchaseId);

    void deleteDocument(Long documentId);
}
