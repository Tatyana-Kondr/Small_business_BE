package de.ait.smallBusiness_be.sales.services.impl;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.sales.models.Sale;
import de.ait.smallBusiness_be.sales.services.DocumentService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import org.springframework.core.io.ClassPathResource;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final SpringTemplateEngine templateEngine;
    // Логотип сохраняем один раз при инициализации
    private String logoPath;

    @PostConstruct
    public void initLogo() {
        try {
            ClassPathResource logoResource = new ClassPathResource("static/images/logo.jpg");
            if (!logoResource.exists()) {
                throw new RestApiException("Логотип не найден в classpath", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            // Берём абсолютный путь (если ресурс реально существует как файл)
            File logoFile = logoResource.getFile();
            this.logoPath = logoFile.toURI().toString();
            System.out.println("PDF logoPath = " + logoPath);
        } catch (Exception e) {
            throw new RestApiException("Ошибка при загрузке логотипа: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

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
        File outputFile = new File(outputPath);

        // Удаляем старый файл, если есть
        if (outputFile.exists() && !outputFile.delete()) {
            throw new RestApiException("Не удалось удалить старый PDF счёт: " + outputPath,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Готовим контекст Thymeleaf
        Context context = new Context();
        context.setVariable("sale", sale);
        context.setVariable("logoPath", logoPath);

        String htmlContent = templateEngine.process("invoice", context);

        // Генерируем PDF
        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
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
        File outputFile = new File(outputPath);

        // Удаляем старый файл, если есть
        if (outputFile.exists() && !outputFile.delete()) {
            throw new RestApiException("Не удалось удалить старую PDF накладную: " + outputPath,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Формируем контекст для Thymeleaf
        Context context = new Context();
        context.setVariable("sale", sale);
        context.setVariable("logoPath", logoPath);

        String htmlContent = templateEngine.process("delivery-bill", context);

        // Генерация PDF
        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
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

    @Override
    public void deleteInvoicePdf(Sale sale, String baseFolder) {
        String invoiceNumber = sale.getInvoiceNumber();
        String year = extractYearFromInvoiceNumber(invoiceNumber);

        String filePath = baseFolder + File.separator + year + File.separator + invoiceNumber + ".pdf";
        File file = new File(filePath);
        if (file.exists() && !file.delete()) {
            throw new RestApiException("Не удалось удалить PDF счёт: " + filePath,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteDeliveryBillPdf(Sale sale, String baseFolder) {
        String deliveryBill = sale.getDeliveryBill();
        String year = extractYearFromInvoiceNumber(deliveryBill);

        String filePath = baseFolder + File.separator + year + File.separator + deliveryBill + ".pdf";
        File file = new File(filePath);
        if (file.exists() && !file.delete()) {
            throw new RestApiException("Не удалось удалить PDF накладную: " + filePath,
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
