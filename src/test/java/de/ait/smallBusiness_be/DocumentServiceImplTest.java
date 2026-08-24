package de.ait.smallBusiness_be;

import de.ait.smallBusiness_be.company.dao.CompanyRepository;
import de.ait.smallBusiness_be.company.model.Company;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.sales.models.Sale;
import de.ait.smallBusiness_be.sales.services.impl.DocumentServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock
    private SpringTemplateEngine templateEngine;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private DocumentServiceImpl documentService;

    private Sale sale;
    private Company company;
    private File logoFile;

    private final String baseFolder = "target/test-docs";

    @BeforeEach
    void setUp() throws IOException {
        logoFile = File.createTempFile("test-logo", ".png");
        logoFile.deleteOnExit();

        company = new Company();
        company.setId(1L);
        company.setLogoUrl(logoFile.getAbsolutePath());

        lenient()
                .when(companyRepository.findById(1L))
                .thenReturn(Optional.of(company));

        sale = new Sale();
        sale.setInvoiceNumber("RE2026001");
        sale.setDeliveryBill("LF2026001");

        deleteTestFolder();
    }

    @AfterEach
    void tearDown() throws IOException {
        deleteTestFolder();
    }

    @Test
    void generateInvoicePdf_success() {
        when(templateEngine.process(
                eq("invoice"),
                any(Context.class)
        )).thenReturn(
                "<html><body><h1>Test Invoice</h1></body></html>"
        );

        assertDoesNotThrow(() ->
                documentService.generateInvoicePdf(
                        sale,
                        baseFolder
                )
        );

        Path expectedPdf = Path.of(
                baseFolder,
                "2026",
                "RE2026001.pdf"
        );

        assertTrue(
                Files.exists(expectedPdf),
                "The invoice PDF must be generated"
        );

        assertTrue(
                expectedPdf.toFile().length() > 0,
                "The generated invoice PDF must not be empty"
        );
    }

    @Test
    void generateDeliveryBillPdf_success() {
        when(templateEngine.process(
                eq("delivery-bill"),
                any(Context.class)
        )).thenReturn(
                "<html><body><h1>Test Delivery Bill</h1></body></html>"
        );

        assertDoesNotThrow(() ->
                documentService.generateDeliveryBillPdf(
                        sale,
                        baseFolder
                )
        );

        Path expectedPdf = Path.of(
                baseFolder,
                "2026",
                "LF2026001.pdf"
        );

        assertTrue(
                Files.exists(expectedPdf),
                "The delivery bill PDF must be generated"
        );

        assertTrue(
                expectedPdf.toFile().length() > 0,
                "The generated delivery bill PDF must not be empty"
        );
    }

    @Test
    void generateInvoiceTempPdf_success() {
        when(templateEngine.process(
                eq("invoice"),
                any(Context.class)
        )).thenReturn(
                "<html><body><h1>Test Invoice</h1></body></html>"
        );

        Path tempFile = documentService.generateInvoiceTempPdf(
                sale,
                baseFolder
        );

        assertNotNull(tempFile);
        assertTrue(Files.exists(tempFile));
        assertTrue(tempFile.getFileName().toString().endsWith(".tmp.pdf"));

        documentService.deleteTempFile(tempFile);

        assertFalse(Files.exists(tempFile));
    }

    @Test
    void generateDeliveryBillTempPdf_success() {
        when(templateEngine.process(
                eq("delivery-bill"),
                any(Context.class)
        )).thenReturn(
                "<html><body><h1>Test Delivery Bill</h1></body></html>"
        );

        Path tempFile =
                documentService.generateDeliveryBillTempPdf(
                        sale,
                        baseFolder
                );

        assertNotNull(tempFile);
        assertTrue(Files.exists(tempFile));
        assertTrue(tempFile.getFileName().toString().endsWith(".tmp.pdf"));

        documentService.deleteTempFile(tempFile);

        assertFalse(Files.exists(tempFile));
    }

    @Test
    void replaceInvoicePdf_success() throws IOException {
        Path yearFolder = Path.of(baseFolder, "2026");
        Files.createDirectories(yearFolder);

        Path oldPdf = yearFolder.resolve("RE2026001.pdf");
        Files.writeString(oldPdf, "old invoice");

        Path tempFile = Files.createTempFile(
                yearFolder,
                "RE2026001-",
                ".tmp.pdf"
        );
        Files.writeString(tempFile, "new invoice");

        documentService.replaceInvoicePdf(
                sale,
                baseFolder,
                tempFile
        );

        assertFalse(Files.exists(tempFile));
        assertTrue(Files.exists(oldPdf));

        assertEquals(
                "new invoice",
                Files.readString(oldPdf)
        );
    }

    @Test
    void replaceDeliveryBillPdf_success() throws IOException {
        Path yearFolder = Path.of(baseFolder, "2026");
        Files.createDirectories(yearFolder);

        Path oldPdf = yearFolder.resolve("LF2026001.pdf");
        Files.writeString(oldPdf, "old delivery bill");

        Path tempFile = Files.createTempFile(
                yearFolder,
                "LF2026001-",
                ".tmp.pdf"
        );
        Files.writeString(tempFile, "new delivery bill");

        documentService.replaceDeliveryBillPdf(
                sale,
                baseFolder,
                tempFile
        );

        assertFalse(Files.exists(tempFile));
        assertTrue(Files.exists(oldPdf));

        assertEquals(
                "new delivery bill",
                Files.readString(oldPdf)
        );
    }

    @Test
    void replaceInvoicePdf_missingTempFile_throws() {
        Path missingTempFile = Path.of(
                baseFolder,
                "missing.tmp.pdf"
        );

        RestApiException exception = assertThrows(
                RestApiException.class,
                () -> documentService.replaceInvoicePdf(
                        sale,
                        baseFolder,
                        missingTempFile
                )
        );

        assertTrue(
                exception.getMessage()
                        .contains("Temporary PDF file does not exist")
        );
    }

    @Test
    void deleteInvoicePdf_success() throws IOException {
        Path yearFolder = Path.of(baseFolder, "2026");
        Files.createDirectories(yearFolder);

        Path pdfFile = yearFolder.resolve(
                sale.getInvoiceNumber() + ".pdf"
        );

        Files.createFile(pdfFile);

        assertDoesNotThrow(() ->
                documentService.deleteInvoicePdf(
                        sale,
                        baseFolder
                )
        );

        assertFalse(
                Files.exists(pdfFile),
                "The invoice PDF must be deleted"
        );
    }

    @Test
    void deleteDeliveryBillPdf_success() throws IOException {
        Path yearFolder = Path.of(baseFolder, "2026");
        Files.createDirectories(yearFolder);

        Path pdfFile = yearFolder.resolve(
                sale.getDeliveryBill() + ".pdf"
        );

        Files.createFile(pdfFile);

        assertDoesNotThrow(() ->
                documentService.deleteDeliveryBillPdf(
                        sale,
                        baseFolder
                )
        );

        assertFalse(
                Files.exists(pdfFile),
                "The delivery bill PDF must be deleted"
        );
    }

    @Test
    void deleteInvoicePdf_fileDoesNotExist_doesNotThrow() {
        assertDoesNotThrow(() ->
                documentService.deleteInvoicePdf(
                        sale,
                        baseFolder
                )
        );
    }

    @Test
    void deleteTempFile_null_doesNotThrow() {
        assertDoesNotThrow(() ->
                documentService.deleteTempFile(null)
        );
    }

    @Test
    void generatePdf_companyNotFound_throws() {
        when(companyRepository.findById(1L))
                .thenReturn(Optional.empty());

        RestApiException exception = assertThrows(
                RestApiException.class,
                () -> documentService.generateInvoicePdf(
                        sale,
                        baseFolder
                )
        );

        assertTrue(
                exception.getMessage().contains("Company not found")
        );
    }

    @Test
    void generatePdf_invalidInvoiceNumber_throws() {
        sale.setInvoiceNumber("INVALID");

        RestApiException exception = assertThrows(
                RestApiException.class,
                () -> documentService.generateInvoicePdf(
                        sale,
                        baseFolder
                )
        );

        assertTrue(
                exception.getMessage()
                        .contains("Invalid document number format")
        );
    }

    private void deleteTestFolder() throws IOException {
        Path root = Path.of(baseFolder);

        if (!Files.exists(root)) {
            return;
        }

        try (var paths = Files.walk(root)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException exception) {
                            throw new RuntimeException(exception);
                        }
                    });
        }
    }
}