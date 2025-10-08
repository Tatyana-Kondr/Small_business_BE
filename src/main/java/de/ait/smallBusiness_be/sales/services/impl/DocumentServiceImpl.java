package de.ait.smallBusiness_be.sales.services.impl;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import de.ait.smallBusiness_be.company.dao.CompanyRepository;
import de.ait.smallBusiness_be.company.model.Company;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.sales.models.Sale;
import de.ait.smallBusiness_be.sales.services.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.ClassPathResource;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final SpringTemplateEngine templateEngine;
    private final CompanyRepository companyRepository;

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
            throw new RestApiException("Не удалось создать папку для документов: " + yearFolderPath,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String outputPath = yearFolderPath + File.separator + documentNumber + ".pdf";
        File outputFile = new File(outputPath);

        if (outputFile.exists() && !outputFile.delete()) {
            throw new RestApiException("Не удалось удалить старый PDF-файл: " + outputPath,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Получаем данные компании
        Company company = companyRepository.findById(1L)
                .orElseThrow(() -> new RestApiException("Компания не найдена", HttpStatus.INTERNAL_SERVER_ERROR));

        // Разрешаем путь логотипа
        String logoPath = resolveCompanyLogoPath(company);

        // Подготавливаем контекст Thymeleaf
        Context context = new Context();
        context.setVariable("sale", sale);
        context.setVariable("company", company);
        context.setVariable("logoPath", logoPath);

        String htmlContent = templateEngine.process(templateName, context);

        // Генерируем PDF
        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, new File(".").toURI().toString());
            builder.toStream(outputStream);
            builder.run();
        } catch (Exception e) {
            throw new RestApiException("Ошибка при создании PDF " + templateName + ": " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // --- Удаление PDF ---
    private void deletePdf(String baseFolder, String documentNumber) {
        String year = extractYearFromInvoiceNumber(documentNumber);
        String filePath = baseFolder + File.separator + year + File.separator + documentNumber + ".pdf";
        File file = new File(filePath);
        if (file.exists() && !file.delete()) {
            throw new RestApiException("Не удалось удалить PDF документ: " + filePath,
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
                    throw new RestApiException(
                            "Логотип компании не найден по пути: " + logoPath,
                            HttpStatus.INTERNAL_SERVER_ERROR
                    );
                }
            }

            throw new RestApiException("Логотип компании не найден", HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            throw new RestApiException(
                    "Ошибка при загрузке логотипа: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }


    private String extractYearFromInvoiceNumber(String invoiceNumber) {
        String[] parts = invoiceNumber.split("-");
        if (parts.length >= 2) {
            return parts[1];
        }
        throw new RestApiException("Неверный формат номера документа: " + invoiceNumber, HttpStatus.BAD_REQUEST);
    }
}
