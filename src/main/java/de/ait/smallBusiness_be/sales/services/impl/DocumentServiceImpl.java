package de.ait.smallBusiness_be.sales.services.impl;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import de.ait.smallBusiness_be.company.dao.CompanyRepository;
import de.ait.smallBusiness_be.company.model.Company;
import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.sales.models.Sale;
import de.ait.smallBusiness_be.sales.models.SaleItem;
import de.ait.smallBusiness_be.sales.services.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final SpringTemplateEngine templateEngine;
    private final CompanyRepository companyRepository;

    private static final Set<Long> EXCLUDED_CATEGORY_IDS = Set.of(5L, 11L, 12L);

    @Override
    public void generateInvoicePdf(Sale sale, String baseFolder) {
        generatePdf(sale, baseFolder, "invoice", sale.getInvoiceNumber());
    }

    @Override
    public void generateDeliveryBillPdf(Sale sale, String baseFolder) {
        generatePdf(sale, baseFolder, "delivery-bill", sale.getDeliveryBill());
    }

    @Override
    public void deleteInvoicePdf(Sale sale, String baseFolder) {
        deletePdf(baseFolder, sale.getInvoiceNumber());
    }

    @Override
    public void deleteDeliveryBillPdf(Sale sale, String baseFolder) {
        deletePdf(baseFolder, sale.getDeliveryBill());
    }

    // --- Общий метод генерации PDF ---
    private void generatePdf(Sale sale, String baseFolder, String templateName, String documentNumber) {
        String year = extractYearFromInvoiceNumber(documentNumber);
        String yearFolderPath = baseFolder + File.separator + year;

        File yearFolder = new File(yearFolderPath);
        if (!yearFolder.exists() && !yearFolder.mkdirs()) {
            throw new RestApiException("Failed to create folder for documents: " + yearFolderPath,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String outputPath = yearFolderPath + File.separator + documentNumber + ".pdf";
        File outputFile = new File(outputPath);

        if (outputFile.exists() && !outputFile.delete()) {
            throw new RestApiException("Failed to delete old PDF file: " + outputPath,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Получаем данные компании
        Company company = companyRepository.findById(1L)
                .orElseThrow(() -> new RestApiException(ErrorDescription.COMPANY_NOT_FOUND, HttpStatus.NOT_FOUND));

        // Пытаемся получить путь логотипа — может быть null
        String logoPath = resolveCompanyLogoPath(company);

        // Подготавливаем контекст Thymeleaf
        Context context = new Context();
        context.setVariable("sale", sale);
        context.setVariable("company", company);
        context.setVariable("logoPath", logoPath);
        if ("delivery-bill".equals(templateName)) {
            context.setVariable("items", printableDeliveryItems(sale));
        } else {
            context.setVariable("items", sale.getSaleItems());
        }

        String htmlContent = templateEngine.process(templateName, context);

        // Генерируем PDF
        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, new File(".").toURI().toString());
            builder.toStream(outputStream);
            builder.run();
        } catch (Exception e) {
            throw new RestApiException("Error while creating PDF " + templateName + ": " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // --- Удаление PDF ---
    private void deletePdf(String baseFolder, String documentNumber) {
        String year = extractYearFromInvoiceNumber(documentNumber);
        String filePath = baseFolder + File.separator + year + File.separator + documentNumber + ".pdf";
        File file = new File(filePath);
        if (file.exists() && !file.delete()) {
            throw new RestApiException("Failed to delete PDF document: " + filePath,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // --- Определение пути логотипа компании ---
    private String resolveCompanyLogoPath(Company company) {
        try {
            if (company.getLogoUrl() != null && !company.getLogoUrl().isEmpty()) {
                // Убираем возможный ведущий слеш, чтобы Paths корректно разрешил путь
                String relativePath = company.getLogoUrl().startsWith("/") ? company.getLogoUrl().substring(1) : company.getLogoUrl();

                Path logoPath = Paths.get(relativePath).toAbsolutePath();
                File logoFile = logoPath.toFile();

                if (logoFile.exists()) {
                    return logoFile.toURI().toString();
                } else {
                    System.out.println("Company logo not found at path: " + logoPath);
                    return null;
                }
            }

            throw new RestApiException(ErrorDescription.LOGO_NOT_FOUND, HttpStatus.NOT_FOUND);

        } catch (Exception e) {
            System.out.println("Error resolving logo path: " + e.getMessage());
        }
        return null;
    }


    private String extractYearFromInvoiceNumber(String invoiceNumber) {
        if (invoiceNumber != null && invoiceNumber.length() == 9) {
            return invoiceNumber.substring(2, 6); // YYYY
        }
        throw new RestApiException("Invalid document number format: " + invoiceNumber, HttpStatus.BAD_REQUEST);
    }

    private List<SaleItem> printableDeliveryItems(Sale sale) {
        if (sale.getSaleItems() == null) return List.of();

        return sale.getSaleItems().stream()
                .filter(i -> i.getProduct() != null)
                .filter(i -> i.getProduct().getProductCategory() != null)
                .filter(i -> {
                    Long catId = i.getProduct().getProductCategory().getId();
                    return catId == null || !EXCLUDED_CATEGORY_IDS.contains(catId);
                })
                .toList();
    }
}
