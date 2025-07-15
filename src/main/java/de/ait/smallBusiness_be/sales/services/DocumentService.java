package de.ait.smallBusiness_be.sales.services;

import de.ait.smallBusiness_be.sales.models.Sale;

public interface DocumentService {
    void generateInvoicePdf(Sale sale, String baseFolder);
    void generateDeliveryBillPdf(Sale sale, String baseFolder);
}
