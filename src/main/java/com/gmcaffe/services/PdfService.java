package com.gmcaffe.services;

import com.gmcaffe.models.Order;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.draw.LineSeparator;

import jakarta.servlet.http.HttpServletResponse;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfService {

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 24, Font.BOLD, new Color(139, 69, 19));
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 14, Font.BOLD, Color.BLACK);
    private static final Font NORMAL_FONT = new Font(Font.HELVETICA, 12, Font.NORMAL, Color.BLACK);
    private static final Font BOLD_FONT = new Font(Font.HELVETICA, 12, Font.BOLD, Color.BLACK);
    private static final Font SMALL_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.GRAY);

    /**
     * Generate a PDF bill for a single order
     */
    public static void generateBillPdf(Order order, HttpServletResponse response) throws Exception {
        Document document = new Document(PageSize.A5);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        PdfWriter.getInstance(document, baos);
        document.open();

        // Header
        Paragraph title = new Paragraph("GM Caffe", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph address = new Paragraph("123 Coffee Street, City\nPhone: +91 7899447884", SMALL_FONT);
        address.setAlignment(Element.ALIGN_CENTER);
        document.add(address);

        document.add(new Paragraph("\n"));

        // Bill Details
        PdfPTable detailsTable = new PdfPTable(2);
        detailsTable.setWidthPercentage(100);
        detailsTable.setSpacingBefore(10);

        addDetailRow(detailsTable, "Bill ID:", order.getBillId());
        addDetailRow(detailsTable, "Date:", order.getOrderedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
        addDetailRow(detailsTable, "Customer:", order.getCustomerName());
        addDetailRow(detailsTable, "Phone:", order.getPhone());
        
        if (order.getAddress() != null && !order.getAddress().trim().isEmpty()) {
            addDetailRow(detailsTable, "Address:", order.getAddress());
        }
        
        addDetailRow(detailsTable, "Order Type:", order.getOrderType());

        document.add(detailsTable);

        // Line separator
        document.add(new LineSeparator());
        document.add(new Paragraph("\n"));

        // Items
        Paragraph itemsHeader = new Paragraph("Order Details", HEADER_FONT);
        document.add(itemsHeader);
        document.add(new Paragraph("\n"));

        Paragraph items = new Paragraph(order.getItems(), NORMAL_FONT);
        document.add(items);

        document.add(new Paragraph("\n"));
        document.add(new LineSeparator());

        // Total
        Paragraph total = new Paragraph("Total: ₹" + order.getTotalAmount(), new Font(Font.HELVETICA, 16, Font.BOLD, Color.BLACK));
        total.setAlignment(Element.ALIGN_RIGHT);
        document.add(total);

        document.add(new Paragraph("\n"));

        // Payment Status
        Paragraph paymentStatus = new Paragraph("Payment Status: " + order.getPaymentStatus(), BOLD_FONT);
        document.add(paymentStatus);

        document.add(new Paragraph("\n\n"));

        // Footer
        Paragraph footer = new Paragraph("Thank you for visiting GM Caffe!\nPlease visit again!", SMALL_FONT);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();

        // Set response headers
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + order.getBillId() + ".pdf");
        response.setContentLength(baos.size());
        response.getOutputStream().write(baos.toByteArray());
        response.getOutputStream().flush();
    }

    /**
     * Generate a PDF report for multiple orders
     */
    public static void generateReportPdf(List<Order> orders, BigDecimal totalRevenue, 
            String periodLabel, HttpServletResponse response) throws Exception {
        
        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        PdfWriter.getInstance(document, baos);
        document.open();

        // Header
        Paragraph title = new Paragraph("GM Caffe - Financial Report", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph period = new Paragraph("Period: " + periodLabel, HEADER_FONT);
        period.setAlignment(Element.ALIGN_CENTER);
        document.add(period);

        document.add(new Paragraph("\n"));

        // Summary
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(60);
        summaryTable.setHorizontalAlignment(Element.ALIGN_CENTER);

        addDetailRow(summaryTable, "Total Orders:", String.valueOf(orders.size()));
        addDetailRow(summaryTable, "Total Revenue:", "₹" + totalRevenue);
        
        double avgOrderValue = orders.isEmpty() ? 0 : totalRevenue.doubleValue() / orders.size();
        addDetailRow(summaryTable, "Avg Order Value:", "₹" + String.format("%.2f", avgOrderValue));

        document.add(summaryTable);
        document.add(new Paragraph("\n"));

        // Orders Table with Address
        PdfPTable ordersTable = new PdfPTable(9);
        ordersTable.setWidthPercentage(100);
        ordersTable.setWidths(new float[]{2, 3, 3, 2, 3, 2, 3, 2, 2});
        
        // Table header
        String[] headers = {"Bill ID", "Date", "Customer", "Phone", "Address", "Amount", "Items", "Payment", "Status"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, BOLD_FONT));
            cell.setBackgroundColor(new Color(255, 230, 230));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            ordersTable.addCell(cell);
        }

        // Table data
        for (Order order : orders) {
            ordersTable.addCell(new PdfPCell(new Phrase(order.getBillId(), NORMAL_FONT)));
            ordersTable.addCell(new PdfPCell(new Phrase(order.getOrderedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")), NORMAL_FONT)));
            ordersTable.addCell(new PdfPCell(new Phrase(order.getCustomerName(), NORMAL_FONT)));
            ordersTable.addCell(new PdfPCell(new Phrase(order.getPhone(), NORMAL_FONT)));
            
            // Address column
            String address = order.getAddress() != null ? order.getAddress() : "-";
            if (address != null && address.length() > 25) {
                address = address.substring(0, 25) + "...";
            }
            ordersTable.addCell(new PdfPCell(new Phrase(address, NORMAL_FONT)));
            
            ordersTable.addCell(new PdfPCell(new Phrase("₹" + order.getTotalAmount(), NORMAL_FONT)));
            
            String items = order.getItems();
            if (items != null && items.length() > 25) {
                items = items.substring(0, 25) + "...";
            }
            ordersTable.addCell(new PdfPCell(new Phrase(items != null ? items : "", NORMAL_FONT)));
            ordersTable.addCell(new PdfPCell(new Phrase(order.getPaymentStatus(), NORMAL_FONT)));
            ordersTable.addCell(new PdfPCell(new Phrase(order.getStatus().name(), NORMAL_FONT)));
        }

        document.add(ordersTable);

        // Footer
        document.add(new Paragraph("\n\n"));
        Paragraph footer = new Paragraph("Generated by GM Caffe Admin", SMALL_FONT);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();

        // Set response headers
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=GM_Caffe_Report.pdf");
        response.setContentLength(baos.size());
        response.getOutputStream().write(baos.toByteArray());
        response.getOutputStream().flush();
    }

    private static void addDetailRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, BOLD_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "", NORMAL_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }
}
