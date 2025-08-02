package de.ait.smallBusiness_be.sales.controllers;

import de.ait.smallBusiness_be.sales.controllers.api.DocumentApi;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sales")
public class DocumentController implements DocumentApi {

    private final String baseInvoiceFolder = "invoices";
    private final String baseDeliveryBillFolder = "delivery-bill";

    @Override
    public ResponseEntity<Resource> getInvoicePdf(String year, String invoiceNumber) {
        return servePdf(baseInvoiceFolder, year, invoiceNumber);
    }

    @Override
    public ResponseEntity<Resource> getDeliveryBillPdf(String year, String deliveryBillNumber) {
        return servePdf(baseDeliveryBillFolder, year, deliveryBillNumber);
    }

    private ResponseEntity<Resource> servePdf(String baseFolder, String year, String fileNameWithoutExt) {
        String filePath = baseFolder + File.separator + year + File.separator + fileNameWithoutExt + ".pdf";
        File file = new File(filePath);

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
}
