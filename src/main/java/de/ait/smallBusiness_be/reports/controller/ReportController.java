package de.ait.smallBusiness_be.reports.controller;

import de.ait.smallBusiness_be.reports.dto.SalesYearReportDto;
import de.ait.smallBusiness_be.reports.pdf.SalesReportPdfService;
import de.ait.smallBusiness_be.reports.service.SalesReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final SalesReportService salesReportService;
    private final SalesReportPdfService salesReportPdfService;

    @GetMapping("/sales")
    public ResponseEntity<List<SalesYearReportDto>> getSalesReport(
            @RequestParam(required = false) Integer year
    ) {

        return ResponseEntity.ok(
                salesReportService.getSalesReport(year)
        );
    }

    @GetMapping(
            value = "/sales/pdf",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<byte[]> getSalesReportPdf(
            @RequestParam(required = false) Integer year
    ) {

        byte[] pdf =
                salesReportPdfService.generateSalesReportPdf(year);

        String fileName =
                year == null
                        ? "Umsatzbericht.pdf"
                        : "Umsatzbericht-" + year + ".pdf";

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + fileName + "\""
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
