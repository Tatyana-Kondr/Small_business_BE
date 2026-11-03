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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


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
            log.warn("Could not delete temporary PDF file: {}", tempFile, e);
        }
    }

    // =========================================================
    // Обычная генерация PDF
    // =========================================================

    private void generatePdf(Sale sale, String baseFolder, String templateName, String documentNumber) {
        Path targetFile = resolveTargetFile(baseFolder, documentNumber);

        renderPdf(sale, templateName, targetFile);
    }

    // =========================================================
    // Временная генерация PDF
    // =========================================================

    private Path generateTempPdf(Sale sale, String baseFolder, String templateName, String documentNumber) {
        Path yearFolder = resolveYearFolder(baseFolder, documentNumber);

        final Path tempFile;

        try {
            tempFile = Files.createTempFile(yearFolder, documentNumber + "-", ".tmp.pdf");
        } catch (IOException e) {
            log.error("Failed to create temporary PDF file for document: {}", documentNumber, e);
            throw new RestApiException(ErrorDescription.FILE_STORAGE_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        try {
            renderPdf(sale, templateName, tempFile);

            return tempFile;

        } catch (RuntimeException e) {
            deleteTempFile(tempFile);
            throw e;
        }
    }

    // =========================================================
    // Рендеринг Thymeleaf -> PDF
    // =========================================================

    private void renderPdf(Sale sale, String templateName, Path outputPath) {
        Company company = companyRepository.findById(1L)
                .orElseThrow(() -> new RestApiException(ErrorDescription.COMPANY_NOT_FOUND, HttpStatus.NOT_FOUND));

        String logoPath = resolveCompanyLogoPath(company);

        Context context = createTemplateContext(sale, company, logoPath, templateName);

        try {
            String htmlContent = templateEngine.process(templateName, context);

            try (OutputStream outputStream = Files.newOutputStream(outputPath)) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                registerFonts(builder);
                builder.withHtmlContent(htmlContent, new File(".").toURI().toString());
                builder.toStream(outputStream);
                builder.run();
            }

        } catch (Exception e) {
            log.error("Failed to generate PDF. Template: {}, saleId: {}, outputPath: {}", templateName, sale.getId(), outputPath, e);
            throw new RestApiException(ErrorDescription.PDF_GENERATION_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Context createTemplateContext(Sale sale, Company company, String logoPath, String templateName) {
        Context context = new Context();

        context.setVariable("sale", sale);
        context.setVariable("company", company);
        context.setVariable("logoPath", logoPath);

        if (DELIVERY_BILL_TEMPLATE.equals(templateName)) {
            context.setVariable("items", printableDeliveryItems(sale));
        } else {
            context.setVariable("items", sale.getSaleItems());
            context.setVariable("taxSummary", calculateTaxSummary(sale));
            context.setVariable("hasDiscount", hasDiscount(sale));
        }

        return context;
    }

    private void registerFonts(PdfRendererBuilder builder) {
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

    private InputStream getRequiredResource(String resourcePath) {
        InputStream inputStream = DocumentServiceImpl.class.getResourceAsStream(resourcePath);

        if (inputStream == null) {
            throw new IllegalStateException("Resource not found: " + resourcePath);
        }

        return inputStream;
    }

    // =========================================================
    // Замена временного PDF на действующий
    // =========================================================

    private void replacePdf(String baseFolder, String documentNumber, Path tempFile) {

        if (tempFile == null || !Files.exists(tempFile)) {
            log.error("Temporary PDF file does not exist: {}", tempFile);
            throw new RestApiException(ErrorDescription.PDF_REPLACEMENT_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Path targetFile = resolveTargetFile(baseFolder, documentNumber);

        try {
            Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

        } catch (AtomicMoveNotSupportedException e) {
            replaceWithoutAtomicMove(tempFile, targetFile);

        } catch (IOException e) {
            log.error("Failed to replace PDF document: {}", targetFile, e);
            throw new RestApiException(ErrorDescription.PDF_REPLACEMENT_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void replaceWithoutAtomicMove(Path tempFile, Path targetFile) {
        try {
            Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to replace PDF document: {}", targetFile, e);
            throw new RestApiException(ErrorDescription.PDF_REPLACEMENT_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // =========================================================
    // Удаление PDF
    // =========================================================

    private void deletePdf(String baseFolder, String documentNumber) {
        Path targetFile = resolveTargetFile(baseFolder, documentNumber);

        try {
            Files.deleteIfExists(targetFile);
        } catch (IOException e) {
            log.error("Failed to delete PDF document: {}", targetFile, e);
            throw new RestApiException(ErrorDescription.FILE_DELETE_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // =========================================================
    // Работа с путями
    // =========================================================

    private Path resolveTargetFile(String baseFolder, String documentNumber) {
        Path yearFolder = resolveYearFolder(baseFolder, documentNumber);

        return yearFolder.resolve(documentNumber + ".pdf");
    }

    private Path resolveYearFolder(String baseFolder, String documentNumber) {
        String year = extractYearFromInvoiceNumber(documentNumber);

        Path yearFolder = Paths.get(baseFolder, year);

        try {
            Files.createDirectories(yearFolder);
            return yearFolder;

        } catch (IOException e) {
            log.error("Failed to create directory: {}", yearFolder, e);
            throw new RestApiException(ErrorDescription.DIRECTORY_CREATION_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // =========================================================
    // Логотип
    // =========================================================

    private String resolveCompanyLogoPath(Company company) {
        String logoUrl = company.getLogoUrl();

        if (logoUrl == null || logoUrl.isBlank()) {
            log.warn("Company logo URL is empty for company ID {}", company.getId());
            return null;
        }

        String relativePath = logoUrl.startsWith("/")
                ? logoUrl.substring(1)
                : logoUrl;

        Path logoPath = Paths.get(relativePath)
                .toAbsolutePath()
                .normalize();

        if (!Files.exists(logoPath)) {
            log.warn("Company logo not found at path: {}", logoPath);
            return null;
        }
        return logoPath.toUri().toString();
    }

    // =========================================================
    // Документный номер
    // =========================================================

    private String extractYearFromInvoiceNumber(String documentNumber) {
        if (documentNumber != null && documentNumber.length() == 9) {
            return documentNumber.substring(2, 6);
        }

        throw new RestApiException("Invalid document number format: " + documentNumber, HttpStatus.BAD_REQUEST
        );
    }

    private Map<BigDecimal, BigDecimal> calculateTaxSummary(Sale sale) {

        Map<BigDecimal, BigDecimal> taxSummary = new TreeMap<>();

        if (sale.getSaleItems() == null) {
            return taxSummary;
        }

        for (SaleItem item : sale.getSaleItems()) {

            if (item.getTax() == null || item.getTaxAmount() == null) {
                continue;
            }

            taxSummary.merge(
                    item.getTax(),
                    item.getTaxAmount(),
                    BigDecimal::add
            );
        }

        taxSummary.replaceAll((taxRate, taxAmount) -> taxAmount.setScale(2, RoundingMode.HALF_UP));

        return taxSummary;
    }

    // =========================================================
    // Позиции Lieferschein
    // =========================================================

    private List<SaleItem> printableDeliveryItems(Sale sale) {
        if (sale.getSaleItems() == null) {
            return List.of();
        }

        return sale.getSaleItems()
                .stream()
                .filter(item -> item.getProduct() != null)
                .filter(item -> item.getProduct().getProductCategory() != null)
                .filter(item -> {
                    Long categoryId = item
                            .getProduct()
                            .getProductCategory()
                            .getId();
                    return categoryId == null || !EXCLUDED_CATEGORY_IDS.contains(categoryId);
                })
                .toList();
    }

    private boolean hasDiscount(Sale sale) {
        return sale.getSaleItems() != null && sale
                .getSaleItems()
                .stream()
                .anyMatch(item -> item.getDiscount() != null && item.getDiscount().compareTo(BigDecimal.ZERO) > 0);
    }
}
