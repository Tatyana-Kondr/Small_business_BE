package de.ait.smallBusiness_be.sales.services.impl;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
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
import java.io.InputStream;
import java.io.OutputStream;
import org.springframework.core.io.ClassPathResource;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final SpringTemplateEngine templateEngine;

    @Override
    public void generateInvoicePdf(Sale sale, String baseFolder) {
        String invoiceNumber = sale.getInvoiceNumber();
        String year = extractYearFromInvoiceNumber(invoiceNumber);

        String yearFolderPath = baseFolder + File.separator + year;
        File yearFolder = new File(yearFolderPath);
        if (!yearFolder.exists() && !yearFolder.mkdirs()) {
            throw new RestApiException("Не удалось создать папку для счетов: " + yearFolderPath,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String outputPath = yearFolderPath + File.separator + invoiceNumber + ".pdf";

        // ✅ Получаем абсолютный file: URI для логотипа
        String logoPath;
        try {
            // Читаем файл логотипа из classpath
            ClassPathResource logoResource = new ClassPathResource("static/images/logo.jpg");
            if (!logoResource.exists()) {
                throw new RestApiException("Логотип не найден в classpath", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // Копируем его во временный файл с .jpg расширением
            File tempLogoFile = File.createTempFile("logo", ".jpg");
            try (InputStream in = logoResource.getInputStream();
                 OutputStream out = new FileOutputStream(tempLogoFile)) {
                in.transferTo(out);
            }

            logoPath = tempLogoFile.toURI().toString(); // file:/... путь
            System.out.println("PDF logoPath = " + logoPath);
        } catch (Exception e) {
            throw new RestApiException("Ошибка при загрузке логотипа: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Context context = new Context();
        context.setVariable("sale", sale);
        context.setVariable("logoPath", logoPath); // передаем абсолютный путь

        String htmlContent = templateEngine.process("invoice", context);

        try (OutputStream outputStream = new FileOutputStream(outputPath)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, new File(".").toURI().toString());
            builder.toStream(outputStream);
            builder.run();
        } catch (Exception e) {
            throw new RestApiException("Ошибка при создании PDF счета: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void generateDeliveryBillPdf(Sale sale, String baseFolder) {
        String deliveryBill = sale.getDeliveryBill();
        String year = extractYearFromInvoiceNumber(deliveryBill);

        String yearFolderPath = baseFolder + File.separator + year;
        File yearFolder = new File(yearFolderPath);
        if (!yearFolder.exists() && !yearFolder.mkdirs()) {
            throw new RestApiException("Не удалось создать папку для накладных: " + yearFolderPath,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String outputPath = yearFolderPath + File.separator + deliveryBill + ".pdf";

        String logoPath;
        try {
            ClassPathResource logoResource = new ClassPathResource("static/images/logo.jpg");
            if (!logoResource.exists()) {
                throw new RestApiException("Логотип не найден в classpath", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            File tempLogoFile = File.createTempFile("logo", ".jpg");
            try (InputStream in = logoResource.getInputStream();
                 OutputStream out = new FileOutputStream(tempLogoFile)) {
                in.transferTo(out);
            }

            logoPath = tempLogoFile.toURI().toString();
            System.out.println("PDF logoPath = " + logoPath);
        } catch (Exception e) {
            throw new RestApiException("Ошибка при загрузке логотипа: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Context context = new Context();
        context.setVariable("sale", sale);
        context.setVariable("logoPath", logoPath);

        String htmlContent = templateEngine.process("delivery-bill", context);

        try (OutputStream outputStream = new FileOutputStream(outputPath)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, new File(".").toURI().toString());
            builder.toStream(outputStream);
            builder.run();
        } catch (Exception e) {
            throw new RestApiException("Ошибка при создании PDF накладной: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String extractYearFromInvoiceNumber(String invoiceNumber) {
        String[] parts = invoiceNumber.split("-");
        if (parts.length >= 2) {
            return parts[1];
        }
        throw new RestApiException("Неверный формат номера счета: " + invoiceNumber, HttpStatus.BAD_REQUEST);
    }
}
