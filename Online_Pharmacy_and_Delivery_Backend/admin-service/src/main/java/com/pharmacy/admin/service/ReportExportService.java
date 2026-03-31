package com.pharmacy.admin.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pharmacy.admin.dto.response.SalesReportDto;

@Service
public class ReportExportService {

    public byte[] toCsv(SalesReportDto report) {
        StringBuilder csv = new StringBuilder();
        csv.append("Metric,Value\n");
        csv.append(csvLine("Start Date", report.getStartDate()));
        csv.append(csvLine("End Date", report.getEndDate()));
        csv.append(csvLine("Generated At", report.getGeneratedAt()));
        csv.append(csvLine("Total Orders", String.valueOf(report.getTotalOrders())));
        csv.append(csvLine("Delivered Orders", String.valueOf(report.getDeliveredOrders())));
        csv.append(csvLine("Cancelled Orders", String.valueOf(report.getCancelledOrders())));
        csv.append(csvLine("Failed Payment Orders", String.valueOf(report.getFailedPaymentOrders())));
        csv.append(csvLine("Total Revenue", String.valueOf(report.getTotalRevenue())));
        csv.append(csvLine("Average Order Value", String.valueOf(report.getAverageOrderValue())));

        csv.append("\nTop Medicines\n");
        csv.append("Name,Quantity Sold,Revenue\n");
        if (report.getTopMedicines() != null) {
            for (SalesReportDto.TopMedicine medicine : report.getTopMedicines()) {
                csv.append(escapeCsv(medicine.getName())).append(',')
                        .append(medicine.getQuantitySold()).append(',')
                        .append(medicine.getRevenue()).append('\n');
            }
        }

        csv.append("\nDaily Revenue\n");
        csv.append("Date,Revenue,Order Count\n");
        if (report.getDailyRevenue() != null) {
            for (SalesReportDto.DailyRevenue day : report.getDailyRevenue()) {
                csv.append(escapeCsv(day.getDate())).append(',')
                        .append(day.getRevenue()).append(',')
                        .append(day.getOrderCount()).append('\n');
            }
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] toPdf(SalesReportDto report) {
        List<String> lines = new ArrayList<>();
        lines.add("Online Pharmacy Sales Report");
        lines.add("Start Date: " + safe(report.getStartDate()));
        lines.add("End Date: " + safe(report.getEndDate()));
        lines.add("Generated At: " + safe(report.getGeneratedAt()));
        lines.add("");
        lines.add("Total Orders: " + report.getTotalOrders());
        lines.add("Delivered Orders: " + report.getDeliveredOrders());
        lines.add("Cancelled Orders: " + report.getCancelledOrders());
        lines.add("Failed Payment Orders: " + report.getFailedPaymentOrders());
        lines.add("Total Revenue: " + report.getTotalRevenue());
        lines.add("Average Order Value: " + report.getAverageOrderValue());

        lines.add("");
        lines.add("Top Medicines:");
        if (report.getTopMedicines() != null && !report.getTopMedicines().isEmpty()) {
            for (SalesReportDto.TopMedicine medicine : report.getTopMedicines()) {
                lines.add("- " + safe(medicine.getName())
                        + " | qty=" + medicine.getQuantitySold()
                        + " | revenue=" + medicine.getRevenue());
            }
        } else {
            lines.add("- None");
        }

        lines.add("");
        lines.add("Daily Revenue:");
        if (report.getDailyRevenue() != null && !report.getDailyRevenue().isEmpty()) {
            for (SalesReportDto.DailyRevenue day : report.getDailyRevenue()) {
                lines.add("- " + safe(day.getDate())
                        + " | revenue=" + day.getRevenue()
                        + " | orders=" + day.getOrderCount());
            }
        } else {
            lines.add("- None");
        }

        return buildSimplePdf(lines);
    }

    private String csvLine(String key, String value) {
        return escapeCsv(key) + ',' + escapeCsv(value) + '\n';
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return '"' + escaped + '"';
    }

    private byte[] buildSimplePdf(List<String> lines) {
        String contentStream = buildContentStream(lines);
        byte[] streamBytes = contentStream.getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> xref = new ArrayList<>();

        write(out, "%PDF-1.4\n");

        xref.add(0);
        xref.add(out.size());
        write(out, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        xref.add(out.size());
        write(out, "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

        xref.add(out.size());
        write(out,
                "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 5 0 R >> >> /Contents 4 0 R >>\nendobj\n");

        xref.add(out.size());
        write(out, "4 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n");
        out.write(streamBytes, 0, streamBytes.length);
        write(out, "\nendstream\nendobj\n");

        xref.add(out.size());
        write(out, "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

        int xrefStart = out.size();
        write(out, "xref\n0 6\n");
        write(out, "0000000000 65535 f \n");
        for (int i = 1; i <= 5; i++) {
            write(out, String.format("%010d 00000 n \n", xref.get(i)));
        }

        write(out, "trailer\n<< /Size 6 /Root 1 0 R >>\n");
        write(out, "startxref\n" + xrefStart + "\n%%EOF");

        return out.toByteArray();
    }

    private String buildContentStream(List<String> lines) {
        StringBuilder builder = new StringBuilder();
        builder.append("BT\n");
        builder.append("/F1 12 Tf\n");

        int y = 800;
        for (String line : lines) {
            if (y < 40) {
                break;
            }
            builder.append("1 0 0 1 40 ").append(y).append(" Tm\n");
            builder.append('(').append(escapePdf(line)).append(") Tj\n");
            y -= 16;
        }

        builder.append("ET");
        return builder.toString();
    }

    private String escapePdf(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }

    private void write(ByteArrayOutputStream out, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        out.write(bytes, 0, bytes.length);
    }
}