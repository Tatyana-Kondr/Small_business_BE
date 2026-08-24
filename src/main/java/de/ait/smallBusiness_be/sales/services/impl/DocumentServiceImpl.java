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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle;

import java.io.*;

import java.nio.file.*;
import java.util.List;
import java.util.Set;


@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final SpringTemplateEngine templateEngine;
    private final CompanyRepository companyRepository;

    private static final Set<Long> EXCLUDED_CATEGORY_IDS = Set.of(5L, 11L, 12L);
    private static final String INVOICE_TEMPLATE = "invoice";
    private static final String DELIVERY_BILL_TEMPLATE = "delivery-bill";

    private static final String REGULAR_FONT_PATH = "/fonts/NotoSans-Regular.ttf";

    private static final String BOLD_FONT_PATH = "/fonts/NotoSans-Bold.ttf";

    private static final String FONT_FAMILY = "Noto Sans";

    @Override
    public void generateInvoicePdf(Sale sale, String baseFolder) {
        generatePdf(sale, baseFolder, INVOICE_TEMPLATE, sale.getInvoiceNumber());
    }

    @Override
    public void generateDeliveryBillPdf(Sale sale, String baseFolder) {
        generatePdf(sale, baseFolder, DELIVERY_BILL_TEMPLATE, sale.getDeliveryBill());
    }

    @Override
    public void deleteInvoicePdf(Sale sale, String baseFolder) {
        deletePdf(baseFolder, sale.getInvoiceNumber());
    }

    @Override
    public void deleteDeliveryBillPdf(Sale sale, String baseFolder) {
        deletePdf(baseFolder, sale.getDeliveryBill());
    }

    @Override
    public Path generateInvoiceTempPdf(Sale sale, String baseFolder) {
        return generateTempPdf(sale, baseFolder, INVOICE_TEMPLATE, sale.getInvoiceNumber());
    }

    @Override
    public Path generateDeliveryBillTempPdf(Sale sale, String baseFolder) {
        return generateTempPdf(sale, baseFolder, DELIVERY_BILL_TEMPLATE, sale.getDeliveryBill());
    }

    @Override
    public void replaceInvoicePdf(Sale sale, String baseFolder, Path tempFile) {
        replacePdf(baseFolder, sale.getInvoiceNumber(), tempFile);
    }

    @Override
    public void replaceDeliveryBillPdf(Sale sale, String baseFolder, Path tempFile) {
        replacePdf(baseFolder, sale.getDeliveryBill(), tempFile);
    }

    @Override
    public void deleteTempFile(Path tempFile) {
        if (tempFile == null) {
            return;
        }

        try {
            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            log.warn(
                    "Could not delete temporary PDF file: {}",
                    tempFile,
                    e
            );
        }
    }

    // =========================================================
    // Обычная генерация PDF
    // =========================================================

    private void generatePdf(
            Sale sale,
            String baseFolder,
            String templateName,
            String documentNumber
    ) {
        Path targetFile = resolveTargetFile(
                baseFolder,
                documentNumber
        );

        renderPdf(
                sale,
                templateName,
                targetFile
        );
    }

    // =========================================================
    // Временная генерация PDF
    // =========================================================

    private Path generateTempPdf(
            Sale sale,
            String baseFolder,
            String templateName,
            String documentNumber
    ) {
        Path yearFolder = resolveYearFolder(
                baseFolder,
                documentNumber
        );

        final Path tempFile;

        try {
            tempFile = Files.createTempFile(
                    yearFolder,
                    documentNumber + "-",
                    ".tmp.pdf"
            );
        } catch (IOException e) {
            throw new RestApiException(
                    "Failed to create temporary PDF file: "
                            + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        try {
            renderPdf(
                    sale,
                    templateName,
                    tempFile
            );

            return tempFile;

        } catch (RuntimeException e) {
            deleteTempFile(tempFile);
            throw e;
        }
    }

    // =========================================================
    // Рендеринг Thymeleaf -> PDF
    // =========================================================

    private void renderPdf(
            Sale sale,
            String templateName,
            Path outputPath
    ) {
        Company company = companyRepository.findById(1L)
                .orElseThrow(() -> new RestApiException(
                        ErrorDescription.COMPANY_NOT_FOUND,
                        HttpStatus.NOT_FOUND
                ));

        String logoPath = resolveCompanyLogoPath(company);

        Context context = createTemplateContext(
                sale,
                company,
                logoPath,
                templateName
        );

        String htmlContent = templateEngine.process(
                templateName,
                context
        );

        try (
                OutputStream outputStream =
                        Files.newOutputStream(outputPath)
        ) {
            PdfRendererBuilder builder =
                    new PdfRendererBuilder();

            builder.useFastMode();

            registerFonts(builder);

            builder.withHtmlContent(
                    htmlContent,
                    new File(".").toURI().toString()
            );

            builder.toStream(outputStream);
            builder.run();

        } catch (Exception e) {
            throw new RestApiException(
                    "Error while creating PDF "
                            + templateName
                            + ": "
                            + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    private Context createTemplateContext(
            Sale sale,
            Company company,
            String logoPath,
            String templateName
    ) {
        Context context = new Context();

        context.setVariable("sale", sale);
        context.setVariable("company", company);
        context.setVariable("logoPath", logoPath);

        if (DELIVERY_BILL_TEMPLATE.equals(templateName)) {
            context.setVariable(
                    "items",
                    printableDeliveryItems(sale)
            );
        } else {
            context.setVariable(
                    "items",
                    sale.getSaleItems()
            );
        }

        return context;
    }

    private void registerFonts(
            PdfRendererBuilder builder
    ) {
        builder.useFont(
                () -> getRequiredResource(REGULAR_FONT_PATH),
                FONT_FAMILY,
                400,
                FontStyle.NORMAL,
                true
        );

        builder.useFont(
                () -> getRequiredResource(BOLD_FONT_PATH),
                FONT_FAMILY,
                700,
                FontStyle.NORMAL,
                true
        );
    }

    private InputStream getRequiredResource(
            String resourcePath
    ) {
        InputStream inputStream =
                DocumentServiceImpl.class
                        .getResourceAsStream(resourcePath);

        if (inputStream == null) {
            throw new IllegalStateException(
                    "Resource not found: " + resourcePath
            );
        }

        return inputStream;
    }

    // =========================================================
    // Замена временного PDF на действующий
    // =========================================================

    private void replacePdf(
            String baseFolder,
            String documentNumber,
            Path tempFile
    ) {
        if (tempFile == null || !Files.exists(tempFile)) {
            throw new RestApiException(
                    "Temporary PDF file does not exist: "
                            + tempFile,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        Path targetFile = resolveTargetFile(
                baseFolder,
                documentNumber
        );

        try {
            Files.move(
                    tempFile,
                    targetFile,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );

        } catch (AtomicMoveNotSupportedException e) {
            replaceWithoutAtomicMove(
                    tempFile,
                    targetFile
            );

        } catch (IOException e) {
            throw new RestApiException(
                    "Failed to replace PDF document "
                            + targetFile
                            + ": "
                            + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    private void replaceWithoutAtomicMove(
            Path tempFile,
            Path targetFile
    ) {
        try {
            Files.move(
                    tempFile,
                    targetFile,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            throw new RestApiException(
                    "Failed to replace PDF document "
                            + targetFile
                            + ": "
                            + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    // =========================================================
    // Удаление PDF
    // =========================================================

    private void deletePdf(
            String baseFolder,
            String documentNumber
    ) {
        Path targetFile = resolveTargetFile(
                baseFolder,
                documentNumber
        );

        try {
            Files.deleteIfExists(targetFile);
        } catch (IOException e) {
            throw new RestApiException(
                    "Failed to delete PDF document: "
                            + targetFile
                            + ". "
                            + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    // =========================================================
    // Работа с путями
    // =========================================================

    private Path resolveTargetFile(
            String baseFolder,
            String documentNumber
    ) {
        Path yearFolder = resolveYearFolder(
                baseFolder,
                documentNumber
        );

        return yearFolder.resolve(
                documentNumber + ".pdf"
        );
    }

    private Path resolveYearFolder(
            String baseFolder,
            String documentNumber
    ) {
        String year = extractYearFromInvoiceNumber(
                documentNumber
        );

        Path yearFolder = Paths.get(
                baseFolder,
                year
        );

        try {
            Files.createDirectories(yearFolder);
            return yearFolder;

        } catch (IOException e) {
            throw new RestApiException(
                    "Failed to create folder for documents: "
                            + yearFolder
                            + ". "
                            + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    // =========================================================
    // Логотип
    // =========================================================

    private String resolveCompanyLogoPath(
            Company company
    ) {
        String logoUrl = company.getLogoUrl();

        if (logoUrl == null || logoUrl.isBlank()) {
            log.warn(
                    "Company logo URL is empty for company ID {}",
                    company.getId()
            );
            return null;
        }

        String relativePath = logoUrl.startsWith("/")
                ? logoUrl.substring(1)
                : logoUrl;

        Path logoPath = Paths.get(relativePath)
                .toAbsolutePath()
                .normalize();

        if (!Files.exists(logoPath)) {
            log.warn(
                    "Company logo not found at path: {}",
                    logoPath
            );
            return null;
        }

        return logoPath.toUri().toString();
    }

    // =========================================================
    // Документный номер
    // =========================================================

    private String extractYearFromInvoiceNumber(
            String documentNumber
    ) {
        if (
                documentNumber != null
                        && documentNumber.length() == 9
        ) {
            return documentNumber.substring(2, 6);
        }

        throw new RestApiException(
                "Invalid document number format: "
                        + documentNumber,
                HttpStatus.BAD_REQUEST
        );
    }

    // =========================================================
    // Позиции Lieferschein
    // =========================================================

    private List<SaleItem> printableDeliveryItems(
            Sale sale
    ) {
        if (sale.getSaleItems() == null) {
            return List.of();
        }

        return sale.getSaleItems()
                .stream()
                .filter(item -> item.getProduct() != null)
                .filter(item ->
                        item.getProduct()
                                .getProductCategory() != null
                )
                .filter(item -> {
                    Long categoryId =
                            item.getProduct()
                                    .getProductCategory()
                                    .getId();

                    return categoryId == null
                            || !EXCLUDED_CATEGORY_IDS.contains(
                            categoryId
                    );
                })
                .toList();
    }

    // --- Общий метод генерации PDF ---
//    private void generatePdf(Sale sale, String baseFolder, String templateName, String documentNumber) {
//        String year = extractYearFromInvoiceNumber(documentNumber);
//        String yearFolderPath = baseFolder + File.separator + year;
//
//        File yearFolder = new File(yearFolderPath);
//        if (!yearFolder.exists() && !yearFolder.mkdirs()) {
//            throw new RestApiException("Failed to create folder for documents: " + yearFolderPath,
//                    HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//
//        String outputPath = yearFolderPath + File.separator + documentNumber + ".pdf";
//        File outputFile = new File(outputPath);
//
//        if (outputFile.exists() && !outputFile.delete()) {
//            throw new RestApiException("Failed to delete old PDF file: " + outputPath,
//                    HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//
//        // Получаем данные компании
//        Company company = companyRepository.findById(1L)
//                .orElseThrow(() -> new RestApiException(ErrorDescription.COMPANY_NOT_FOUND, HttpStatus.NOT_FOUND));
//
//        // Пытаемся получить путь логотипа — может быть null
//        String logoPath = resolveCompanyLogoPath(company);
//
//        // Подготавливаем контекст Thymeleaf
//        Context context = new Context();
//        context.setVariable("sale", sale);
//        context.setVariable("company", company);
//        context.setVariable("logoPath", logoPath);
//        if ("delivery-bill".equals(templateName)) {
//            context.setVariable("items", printableDeliveryItems(sale));
//        } else {
//            context.setVariable("items", sale.getSaleItems());
//        }
//
//        String htmlContent = templateEngine.process(templateName, context);
//
//        // Генерируем PDF
//        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
//            PdfRendererBuilder builder = new PdfRendererBuilder();
//            builder.useFastMode();
//            builder.useFont(
//                    () -> getRequiredResource("/fonts/NotoSans-Regular.ttf"),
//                    "Noto Sans",
//                    400,
//                    FontStyle.NORMAL,
//                    true
//            );
//
//            builder.useFont(
//                    () -> getRequiredResource("/fonts/NotoSans-Bold.ttf"),
//                    "Noto Sans",
//                    700,
//                    FontStyle.NORMAL,
//                    true
//            );
//            builder.withHtmlContent(htmlContent, new File(".").toURI().toString());
//            builder.toStream(outputStream);
//            builder.run();
//        } catch (Exception e) {
//            throw new RestApiException("Error while creating PDF " + templateName + ": " + e.getMessage(),
//                    HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    private InputStream getRequiredResource(String resourcePath) {
//        InputStream inputStream =
//                DocumentServiceImpl.class.getResourceAsStream(resourcePath);
//
//        if (inputStream == null) {
//            throw new IllegalStateException(
//                    "Resource not found: " + resourcePath
//            );
//        }
//
//        return inputStream;
//    }
//
//    // --- Удаление PDF ---
//    private void deletePdf(String baseFolder, String documentNumber) {
//        String year = extractYearFromInvoiceNumber(documentNumber);
//        String filePath = baseFolder + File.separator + year + File.separator + documentNumber + ".pdf";
//        File file = new File(filePath);
//        if (file.exists() && !file.delete()) {
//            throw new RestApiException("Failed to delete PDF document: " + filePath,
//                    HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    // --- Определение пути логотипа компании ---
//    private String resolveCompanyLogoPath(Company company) {
//        try {
//            if (company.getLogoUrl() != null && !company.getLogoUrl().isEmpty()) {
//                // Убираем возможный ведущий слеш, чтобы Paths корректно разрешил путь
//                String relativePath = company.getLogoUrl().startsWith("/") ? company.getLogoUrl().substring(1) : company.getLogoUrl();
//
//                Path logoPath = Paths.get(relativePath).toAbsolutePath();
//                File logoFile = logoPath.toFile();
//
//                if (logoFile.exists()) {
//                    return logoFile.toURI().toString();
//                } else {
//                    System.out.println("Company logo not found at path: " + logoPath);
//                    return null;
//                }
//            }
//
//            throw new RestApiException(ErrorDescription.LOGO_NOT_FOUND, HttpStatus.NOT_FOUND);
//
//        } catch (Exception e) {
//            System.out.println("Error resolving logo path: " + e.getMessage());
//        }
//        return null;
//    }
//
//
//    private String extractYearFromInvoiceNumber(String invoiceNumber) {
//        if (invoiceNumber != null && invoiceNumber.length() == 9) {
//            return invoiceNumber.substring(2, 6); // YYYY
//        }
//        throw new RestApiException("Invalid document number format: " + invoiceNumber, HttpStatus.BAD_REQUEST);
//    }
//
//    private List<SaleItem> printableDeliveryItems(Sale sale) {
//        if (sale.getSaleItems() == null) return List.of();
//
//        return sale.getSaleItems().stream()
//                .filter(i -> i.getProduct() != null)
//                .filter(i -> i.getProduct().getProductCategory() != null)
//                .filter(i -> {
//                    Long catId = i.getProduct().getProductCategory().getId();
//                    return catId == null || !EXCLUDED_CATEGORY_IDS.contains(catId);
//                })
//                .toList();
//    }
}
