package com.gmcaffe.services;

import com.gmcaffe.models.Order;

import jakarta.servlet.http.HttpServletResponse;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * PDF Service for generating bills and reports using OpenPDF.
 */
public class PdfService {

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 18, Font.BOLD);
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 12, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL);
    private static final Font SMALL_FONT = new Font(Font.HELVETICA, 8, Font.NORMAL);

    /**
     * Generate a bill for a single order as PDF
     */
    public static void generateBillPdf(Order order, HttpServletResponse response) throws Exception {
        Document document = new Document(PageSize.A4);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + 
            (order.getBillId() != null ? order.getBillId() : "bill") + ".pdf");
        
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();
        
        // Title
        Paragraph title = new Paragraph("GM Caffe", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        
        Paragraph address = new Paragraph("Ashok Nagar, Devanahalli\nVijayapura Town, Karnataka 562135\nPhone: +91 7899447884", NORMAL_FONT);
        address.setAlignment(Element.ALIGN_CENTER);
        document.add(address);
        
        document.add(new Paragraph("\n"));
        
        // Bill details
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        
        addTableRow(table, "Bill ID:", safeString(order.getBillId()));
        addTableRow(table, "Date:", order.getOrderedAt() != null ? 
            order.getOrderedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) : "");
        addTableRow(table, "Customer:", safeString(order.getCustomerName()));
        addTableRow(table, "Phone:", safeString(order.getPhone()));
        
        if (order.getAddress() != null && !order.getAddress().trim().isEmpty()) {
            addTableRow(table, "Address:", safeString(order.getAddress()));
        }
        
        document.add(table);
        document.add(new Paragraph("\n"));
        
        // Order items
        Paragraph itemsHeader = new Paragraph("Order Details", HEADER_FONT);
        document.add(itemsHeader);
        document.add(new Paragraph(safeString(order.getItems()), NORMAL_FONT));
        document.add(new Paragraph("\n"));
        
        // Total
        Paragraph total = new Paragraph("Total Amount: Rs." + safeAmount(order.getTotalAmount()), HEADER_FONT);
        document.add(total);
        
        PdfPTable statusTable = new PdfPTable(2);
        statusTable.setWidthPercentage(100);
        addTableRow(statusTable, "Payment Status:", safeString(order.getPaymentStatus()));
        addTableRow(statusTable, "Order Type:", safeString(order.getOrderType()));
        document.add(statusTable);
        
        // Footer
        document.add(new Paragraph("\n\n"));
        Paragraph footer = new Paragraph("Thank you for visiting GM Caffe!\nPlease visit again!", NORMAL_FONT);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
        
        document.close();
    }

    /**
     * Generate a financial report for multiple orders as PDF
     */
    public static void generateReportPdf(List<Order> orders, BigDecimal totalRevenue, 
            String periodLabel, HttpServletResponse response) throws Exception {
        
        Document document = new Document(PageSize.A4.rotate());
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=GM_Caffe_Report.pdf");
        
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();
        
        // Title
        Paragraph title = new Paragraph("GM Caffe - Financial Report", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        
        Paragraph period = new Paragraph("Period: " + periodLabel, NORMAL_FONT);
        period.setAlignment(Element.ALIGN_CENTER);
        document.add(period);
        
        document.add(new Paragraph("\n"));
        
        // Summary
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(60);
        summaryTable.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        addTableRow(summaryTable, "Total Orders:", String.valueOf(orders.size()));
        addTableRow(summaryTable, "Total Revenue:", "Rs." + safeAmount(totalRevenue));
        
        if (!orders.isEmpty() && totalRevenue != null) {
            double avgOrderValue = totalRevenue.doubleValue() / orders.size();
            addTableRow(summaryTable, "Average Order Value:", "Rs." + String.format("%.2f", avgOrderValue));
        }
        
        long paidCount = orders.stream()
            .filter(o -> "PAID".equals(o.getPaymentStatus()))
            .count();
        addTableRow(summaryTable, "Paid Orders:", String.valueOf(paidCount));
        addTableRow(summaryTable, "Pending Orders:", String.valueOf(orders.size() - paidCount));
        
        document.add(summaryTable);
        document.add(new Paragraph("\n"));
        
        // Orders table
        Paragraph ordersHeader = new Paragraph("Order Details", HEADER_FONT);
        document.add(ordersHeader);
        
        PdfPTable ordersTable = new PdfPTable(6);
        ordersTable.setWidthPercentage(100);
        ordersTable.setWidths(new float[]{15, 20, 20, 25, 15, 12});
        
        // Header row
        String[] headers = {"Bill ID", "Date", "Customer", "Items", "Amount", "Payment"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
            cell.setBackgroundColor(new java.awt.Color(230, 230, 230));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            ordersTable.addCell(cell);
        }
        
        // Data rows
        for (Order order : orders) {
            ordersTable.addCell(new Phrase(safeString(order.getBillId()), SMALL_FONT));
            ordersTable.addCell(new Phrase(order.getOrderedAt() != null ? 
                order.getOrderedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) : "", SMALL_FONT));
            ordersTable.addCell(new Phrase(safeString(order.getCustomerName()), SMALL_FONT));
            
            String items = safeString(order.getItems());
            if (items.length() > 50) items = items.substring(0, 47) + "...";
            ordersTable.addCell(new Phrase(items, SMALL_FONT));
            
            ordersTable.addCell(new Phrase("Rs." + safeAmount(order.getTotalAmount()), SMALL_FONT));
            ordersTable.addCell(new Phrase(safeString(order.getPaymentStatus()), SMALL_FONT));
        }
        
        document.add(ordersTable);
        
        // Footer
        document.add(new Paragraph("\n\n"));
        Paragraph footer = new Paragraph("Generated by GM Caffe Admin", SMALL_FONT);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
        
        document.close();
    }
    
    // Helper methods
    private static void addTableRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, HEADER_FONT));
        labelCell.setBorder(PdfPCell.NO_BORDER);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "", NORMAL_FONT));
        valueCell.setBorder(PdfPCell.NO_BORDER);
        table.addCell(valueCell);
    }
    
    private static String safeString(String value) {
        return value != null ? value : "";
    }
    
    private static String safeAmount(BigDecimal value) {
        return value != null ? value.toString() : "0.00";
    }
}

