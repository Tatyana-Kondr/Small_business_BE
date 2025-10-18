package de.ait.smallBusiness_be;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.thymeleaf.context.Context;
import de.ait.smallBusiness_be.company.dao.CompanyRepository;
import de.ait.smallBusiness_be.company.model.Company;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.sales.models.Sale;
import de.ait.smallBusiness_be.sales.services.impl.DocumentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock
    private SpringTemplateEngine templateEngine;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private DocumentServiceImpl documentService;

     Sale sale;
    Company company;
     File logoFile;

     String baseFolder = "target/test-docs";

    @BeforeEach
    void setUp() throws IOException {
        // Создаем временный файл логотипа
        logoFile = File.createTempFile("test-logo", ".png");
        logoFile.deleteOnExit();

        company = new Company();
        company.setId(1L);
        company.setLogoUrl(logoFile.getAbsolutePath());

        // Используем lenient(), чтобы Mockito не ругался на "ненужный stub"
        lenient().when(companyRepository.findById(1L)).thenReturn(Optional.of(company));

        sale = new Sale();
        sale.setInvoiceNumber("INV-001");
        sale.setDeliveryBill("DB-001");

        // Убираем старые временные файлы
        File dir = new File("target/test-docs/001");
        dir.mkdirs();

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
        dir.delete();
    }

    @Test
    void generateInvoicePdf_success() {
        when(templateEngine.process(eq("invoice"), any(Context.class)))
                .thenReturn("<html><body><h1>Test Invoice</h1></body></html>");

        assertDoesNotThrow(() -> documentService.generateInvoicePdf(sale, baseFolder));

        File yearFolder = new File(baseFolder, "001");
        File expectedPdf = new File(yearFolder, "INV-001.pdf");
        assertTrue(expectedPdf.exists(), "The PDF must be generated");

        // Чистим после теста
        expectedPdf.delete();
        yearFolder.delete();
    }

    @Test
    void generateDeliveryBillPdf_success() {
        when(templateEngine.process(eq("delivery-bill"), any(Context.class)))
                .thenReturn("<html><body><h1>Test Delivery Bill</h1></body></html>");

        assertDoesNotThrow(() -> documentService.generateDeliveryBillPdf(sale, baseFolder));

        File yearFolder = new File(baseFolder, "001");
        File expectedPdf = new File(yearFolder, "DB-001.pdf");
        assertTrue(expectedPdf.exists(), "The PDF must be generated");

        expectedPdf.delete();
        yearFolder.delete();
    }

    @Test
    void deleteInvoicePdf_success() throws IOException {
        File dir = new File(baseFolder + "/001");
        dir.mkdirs();
        File file = new File(dir, sale.getInvoiceNumber() + ".pdf");
        file.createNewFile();

        assertDoesNotThrow(() -> documentService.deleteInvoicePdf(sale, baseFolder));
        assertFalse(file.exists(), "The PDF must be deleted");

        dir.delete();
    }

    @Test
    void deleteDeliveryBillPdf_success() throws IOException {
        File dir = new File(baseFolder + "/001");
        dir.mkdirs();
        File file = new File(dir, sale.getDeliveryBill() + ".pdf");
        file.createNewFile();

        assertDoesNotThrow(() -> documentService.deleteDeliveryBillPdf(sale, baseFolder));
        assertFalse(file.exists(), "The PDF must be deleted");

        dir.delete();
    }

    @Test
    void generatePdf_companyNotFound_throws() {
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

        RestApiException ex = assertThrows(RestApiException.class,
                () -> documentService.generateInvoicePdf(sale, baseFolder));

        assertTrue(ex.getMessage().contains("Company not found"));
    }

    @Test
    void generatePdf_invalidInvoiceNumber_throws() {
        sale.setInvoiceNumber("INVALID");

        RestApiException ex = assertThrows(RestApiException.class,
                () -> documentService.generateInvoicePdf(sale, baseFolder));

        assertTrue(ex.getMessage().contains("Invalid document number format"));
    }
}

