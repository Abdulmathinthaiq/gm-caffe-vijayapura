package com.gmcaffe.services;

import com.gmcaffe.models.Order;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * PDF Service for generating bills and reports.
 * Uses a simple text-based format that works reliably without external PDF dependencies.
 */
public class PdfService {

    /**
     * Generate a bill for a single order (text format for reliability)
     */
    public static void generateBillPdf(Order order, HttpServletResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();
        
        sb.append("========================================\n");
        sb.append("            GM Caffe\n");
        sb.append("========================================\n\n");
        sb.append("Ashok Nagar, Devanahalli\n");
        sb.append("Vijayapura Town, Karnataka 562135\n");
        sb.append("Phone: +91 7899447884\n\n");
        
        sb.append("----------------------------------------\n");
        sb.append("BILL RECEIPT\n");
        sb.append("----------------------------------------\n\n");
        
        sb.append("Bill ID: ").append(safeString(order.getBillId())).append("\n");
        sb.append("Date: ").append(order.getOrderedAt() != null ? 
            order.getOrderedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) : "").append("\n");
        sb.append("Customer: ").append(safeString(order.getCustomerName())).append("\n");
        sb.append("Phone: ").append(safeString(order.getPhone())).append("\n");
        
        if (order.getAddress() != null && !order.getAddress().trim().isEmpty()) {
            sb.append("Address: ").append(safeString(order.getAddress())).append("\n");
        }
        
        sb.append("\n----------------------------------------\n");
        sb.append("ORDER DETAILS\n");
        sb.append("----------------------------------------\n");
        sb.append(safeString(order.getItems())).append("\n");
        
        sb.append("----------------------------------------\n");
        sb.append("Total Amount: ₹").append(safeAmount(order.getTotalAmount())).append("\n");
        sb.append("Payment Status: ").append(safeString(order.getPaymentStatus())).append("\n");
        sb.append("Order Type: ").append(safeString(order.getOrderType())).append("\n");
        
        sb.append("\n========================================\n");
        sb.append("Thank you for visiting GM Caffe!\n");
        sb.append("Please visit again!\n");
        sb.append("========================================\n");

        // Set response as text file that can be opened as plain text
        response.setContentType("text/plain;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + 
            (order.getBillId() != null ? order.getBillId() : "bill") + ".txt");
        response.getWriter().write(sb.toString());
    }

    /**
     * Generate a financial report for multiple orders
     */
    public static void generateReportPdf(List<Order> orders, BigDecimal totalRevenue, 
            String periodLabel, HttpServletResponse response) throws IOException {
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("================================================================\n");
        sb.append("            GM Caffe - Financial Report\n");
        sb.append("================================================================\n\n");
        
        sb.append("Period: ").append(periodLabel).append("\n\n");
        
        // Summary
        sb.append("----------------------------------------\n");
        sb.append("SUMMARY\n");
        sb.append("----------------------------------------\n");
        sb.append("Total Orders: ").append(orders.size()).append("\n");
        sb.append("Total Revenue: ₹").append(safeAmount(totalRevenue)).append("\n");
        
        if (!orders.isEmpty() && totalRevenue != null) {
            double avgOrderValue = totalRevenue.doubleValue() / orders.size();
            sb.append("Average Order Value: ₹").append(String.format("%.2f", avgOrderValue)).append("\n");
        }
        
        // Count by payment status
        long paidCount = orders.stream()
            .filter(o -> "PAID".equals(o.getPaymentStatus()))
            .count();
        long pendingCount = orders.size() - paidCount;
        
        sb.append("\nPayment Status:\n");
        sb.append("  - Paid: ").append(paidCount).append("\n");
        sb.append("  - Pending: ").append(pendingCount).append("\n");
        
        // Count by order type
        long onlineCount = orders.stream()
            .filter(o -> "ONLINE".equals(o.getOrderType()))
            .count();
        long offlineCount = orders.size() - onlineCount;
        
        sb.append("\nOrder Type:\n");
        sb.append("  - Online: ").append(onlineCount).append("\n");
        sb.append("  - Offline: ").append(offlineCount).append("\n");
        
        sb.append("\n================================================================\n");
        sb.append("ORDER DETAILS\n");
        sb.append("================================================================\n\n");
        
        for (Order order : orders) {
            sb.append("----------------------------------------\n");
            sb.append("Bill ID: ").append(safeString(order.getBillId())).append("\n");
            sb.append("Date: ").append(order.getOrderedAt() != null ? 
                order.getOrderedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) : "").append("\n");
            sb.append("Customer: ").append(safeString(order.getCustomerName())).append("\n");
            sb.append("Phone: ").append(safeString(order.getPhone())).append("\n");
            sb.append("Amount: ₹").append(safeAmount(order.getTotalAmount())).append("\n");
            sb.append("Items: ").append(safeString(order.getItems())).append("\n");
            sb.append("Payment: ").append(safeString(order.getPaymentStatus())).append("\n");
            sb.append("Status: ").append(order.getStatus() != null ? order.getStatus().name() : "").append("\n");
            sb.append("----------------------------------------\n\n");
        }
        
        sb.append("================================================================\n");
        sb.append("Generated by GM Caffe Admin\n");
        sb.append("================================================================\n");

        response.setContentType("text/plain;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=GM_Caffe_Report.txt");
        response.getWriter().write(sb.toString());
    }
    
    // Helper methods to prevent NullPointerException
    private static String safeString(String value) {
        return value != null ? value : "";
    }
    
    private static String safeAmount(BigDecimal value) {
        return value != null ? value.toString() : "0.00";
    }
}

