package de.ait.smallBusiness_be.sales.services;

import de.ait.smallBusiness_be.sales.models.Sale;
import java.nio.file.Path;

public interface DocumentService {
    void generateInvoicePdf(Sale sale, String baseFolder);
    void generateDeliveryBillPdf(Sale sale, String baseFolder);
    void deleteInvoicePdf(Sale sale, String baseFolder);
    void deleteDeliveryBillPdf(Sale sale, String baseFolder);
    Path generateInvoiceTempPdf(Sale sale, String baseFolder);
    Path generateDeliveryBillTempPdf(Sale sale, String baseFolder);
    void replaceInvoicePdf(Sale sale, String baseFolder, Path tempFile);
    void replaceDeliveryBillPdf(Sale sale, String baseFolder, Path tempFile);
    void deleteTempFile(Path tempFile);
}
