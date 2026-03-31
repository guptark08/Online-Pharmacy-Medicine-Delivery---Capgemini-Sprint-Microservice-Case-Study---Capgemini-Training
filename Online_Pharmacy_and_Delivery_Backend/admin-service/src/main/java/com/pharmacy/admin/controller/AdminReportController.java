package com.pharmacy.admin.controller;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacy.admin.dto.response.ApiResponse;
import com.pharmacy.admin.dto.response.InventoryReportDto;
import com.pharmacy.admin.dto.response.SalesReportDto;
import com.pharmacy.admin.exception.BadRequestException;
import com.pharmacy.admin.service.AdminReportService;
import com.pharmacy.admin.service.ReportExportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Reports", description = "Sales, inventory, and prescription reports for admin")
public class AdminReportController {

    private final AdminReportService reportService;
    private final ReportExportService reportExportService;

    @GetMapping("/sales")
    @Operation(summary = "Sales report for a date range")
    public ResponseEntity<ApiResponse<SalesReportDto>> getSalesReport(
            @RequestParam @Parameter(description = "Start date (yyyy-MM-dd)") String startDate,
            @RequestParam @Parameter(description = "End date (yyyy-MM-dd)") String endDate) {

        return ResponseEntity.ok(ApiResponse.success(
                "Sales report generated",
                reportService.getSalesReport(startDate, endDate)));
    }

    @GetMapping("/sales/today")
    @Operation(summary = "Today's sales report")
    public ResponseEntity<ApiResponse<SalesReportDto>> getTodaySalesReport() {
        String today = LocalDate.now().toString();
        return ResponseEntity.ok(ApiResponse.success(
                "Today's report generated",
                reportService.getSalesReport(today, today)));
    }

    @GetMapping("/sales/this-month")
    @Operation(summary = "Current month's sales report")
    public ResponseEntity<ApiResponse<SalesReportDto>> getThisMonthSalesReport() {
        String startDate = LocalDate.now().withDayOfMonth(1).toString();
        String endDate = LocalDate.now().toString();

        return ResponseEntity.ok(ApiResponse.success(
                "This month's report generated",
                reportService.getSalesReport(startDate, endDate)));
    }

    @GetMapping("/export")
    @Operation(summary = "Export sales report as CSV or PDF")
    public ResponseEntity<byte[]> exportReport(
            @RequestParam String format,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDate resolvedEnd = endDate == null || endDate.isBlank() ? LocalDate.now() : parseDate(endDate, "endDate");
        LocalDate resolvedStart = startDate == null || startDate.isBlank()
                ? resolvedEnd.minusDays(30)
                : parseDate(startDate, "startDate");

        String start = resolvedStart.toString();
        String end = resolvedEnd.toString();
        SalesReportDto report = reportService.getSalesReport(start, end);

        String normalizedFormat = format == null ? "" : format.trim().toLowerCase(Locale.ROOT);
        byte[] data;
        MediaType mediaType;
        String extension;

        switch (normalizedFormat) {
            case "csv" -> {
                data = reportExportService.toCsv(report);
                mediaType = MediaType.parseMediaType("text/csv");
                extension = "csv";
            }
            case "pdf" -> {
                data = reportExportService.toPdf(report);
                mediaType = MediaType.APPLICATION_PDF;
                extension = "pdf";
            }
            default -> throw new BadRequestException("Invalid format. Supported values: csv, pdf");
        }

        String filename = "sales-report-" + start + "_to_" + end + "." + extension;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(data);
    }

    @GetMapping("/inventory")
    @Operation(summary = "Full inventory report")
    public ResponseEntity<ApiResponse<InventoryReportDto>> getInventoryReport() {
        return ResponseEntity.ok(ApiResponse.success(
                "Inventory report generated",
                reportService.getInventoryReport()));
    }

    @GetMapping("/prescriptions")
    @Operation(summary = "Prescription volume report")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getPrescriptionReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        return ResponseEntity.ok(ApiResponse.success(
                "Prescription report generated",
                reportService.getPrescriptionVolumeReport(startDate, endDate)));
    }

    private LocalDate parseDate(String value, String fieldName) {
        try {
            return LocalDate.parse(value);
        } catch (Exception ex) {
            throw new BadRequestException("Invalid " + fieldName + " format. Use yyyy-MM-dd");
        }
    }
}