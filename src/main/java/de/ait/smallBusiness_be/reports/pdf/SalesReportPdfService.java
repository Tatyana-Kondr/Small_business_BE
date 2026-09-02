package de.ait.smallBusiness_be.reports.pdf;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import de.ait.smallBusiness_be.reports.dto.SalesYearReportDto;
import de.ait.smallBusiness_be.reports.service.SalesReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesReportPdfService {

    private final SalesReportService salesReportService;
    private final TemplateEngine templateEngine;

    public byte[] generateSalesReportPdf(Integer year) {

        List<SalesYearReportDto> report =
                salesReportService.getSalesReport(year);

        Context context = new Context();

        context.setVariable("reports", report);
        context.setVariable("selectedYear", year);

        String html =
                templateEngine.process("sales-report", context);

        try (ByteArrayOutputStream outputStream =
                     new ByteArrayOutputStream()) {

            PdfRendererBuilder builder =
                    new PdfRendererBuilder();

            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);

            builder.run();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Fehler beim Erstellen des Umsatzbericht-PDF.",
                    e
            );
        }
    }
}