package com.gmcaffe.controllers;

import com.gmcaffe.models.Gallery;
import com.gmcaffe.models.MenuItem;
import com.gmcaffe.models.Offer;
import com.gmcaffe.models.Order;
import com.gmcaffe.models.Review;
import com.gmcaffe.models.User;
import com.gmcaffe.repositories.GalleryRepository;
import com.gmcaffe.repositories.MenuItemRepository;
import com.gmcaffe.repositories.OfferRepository;
import com.gmcaffe.repositories.OrderRepository;
import com.gmcaffe.repositories.ReviewRepository;
import com.gmcaffe.repositories.UserRepository;
import com.gmcaffe.services.PdfService;

import jakarta.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    private final MenuItemRepository menuItemRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final OfferRepository offerRepository;
    private final GalleryRepository galleryRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    private ServletContext servletContext;
    
    public AdminController(MenuItemRepository menuItemRepository,
                          OrderRepository orderRepository,
                          ReviewRepository reviewRepository,
                          UserRepository userRepository,
                          OfferRepository offerRepository,
                          GalleryRepository galleryRepository,
                          PasswordEncoder passwordEncoder) {

        this.menuItemRepository = menuItemRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.offerRepository = offerRepository;
        this.galleryRepository = galleryRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @GetMapping("/login")
    public String login() {
        return "admin/login";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Get totals with null safety
        long totalOrders = 0;
        long pendingOrders = 0;
        long totalMenuItems = 0;
        long newReviews = 0;
        double avgRating = 0.0;
        
        try {
            Long total = orderRepository.countTotal();
            totalOrders = total != null ? total : 0;
        } catch (Exception e) {
            System.err.println("Error getting total orders: " + e.getMessage());
        }
        
        try {
            Long pending = orderRepository.countByStatus(Order.OrderStatus.PENDING);
            pendingOrders = pending != null ? pending : 0;
        } catch (Exception e) {
            System.err.println("Error getting pending orders: " + e.getMessage());
        }
        
        try {
            Long count = menuItemRepository.count();
            totalMenuItems = count != null ? count : 0;
        } catch (Exception e) {
            System.err.println("Error getting menu items count: " + e.getMessage());
        }
        
        try {
            Long reviews = reviewRepository.countByApprovedFalse();
            newReviews = reviews != null ? reviews : 0;
        } catch (Exception e) {
            System.err.println("Error getting pending reviews: " + e.getMessage());
        }
        
        try {
            Double rating = reviewRepository.getAverageRating();
            avgRating = rating != null ? rating : 0.0;
        } catch (Exception e) {
            System.err.println("Error getting average rating: " + e.getMessage());
        }
        
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("totalMenuItems", totalMenuItems);
        model.addAttribute("newReviews", newReviews);
        model.addAttribute("avgRating", avgRating);
        
        return "admin/dashboard";
    }
    
    // Menu Management
    @GetMapping("/menu")
    public String manageMenu(Model model) {
        try {
            List<MenuItem> items = menuItemRepository.findAll();
            model.addAttribute("items", items != null ? items : Collections.emptyList());
        } catch (Exception e) {
            model.addAttribute("items", Collections.emptyList());
        }
        model.addAttribute("item", new MenuItem());
        return "admin/menu";
    }
    
    @PostMapping("/menu/save")
    public String saveMenu(@ModelAttribute MenuItem item, RedirectAttributes redirectAttributes) {
        try {
            // Validate required fields
            if (item.getName() == null || item.getName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Menu item name is required.");
                return "redirect:/admin/menu";
            }
            if (item.getPrice() == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Price is required.");
                return "redirect:/admin/menu";
            }
            
            if (item.getId() != null) {
                MenuItem existing = menuItemRepository.findById(item.getId()).orElse(item);
                item.setImageUrl(existing.getImageUrl());
            }
            menuItemRepository.save(item);
            redirectAttributes.addFlashAttribute("successMessage", "Menu item saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving menu item: " + e.getMessage());
        }
        return "redirect:/admin/menu";
    }
    
    @GetMapping("/menu/edit/{id}")
    public String editMenu(@PathVariable Long id, Model model) {
        try {
            MenuItem item = menuItemRepository.findById(id).orElse(new MenuItem());
            model.addAttribute("item", item);
        } catch (Exception e) {
            model.addAttribute("item", new MenuItem());
        }
        
        try {
            List<MenuItem> items = menuItemRepository.findAll();
            model.addAttribute("items", items != null ? items : Collections.emptyList());
        } catch (Exception e) {
            model.addAttribute("items", Collections.emptyList());
        }
        return "admin/menu";
    }
    
    @GetMapping("/menu/delete/{id}")
    public String deleteMenu(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            menuItemRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Menu item deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting menu item: " + e.getMessage());
        }
        return "redirect:/admin/menu";
    }
    
    // Order Management
    @GetMapping("/orders")
    public String manageOrders(Model model) {
        try {
            List<Order> orders = orderRepository.findAllByOrderByOrderedAtDesc();
            model.addAttribute("orders", orders != null ? orders : Collections.emptyList());
        } catch (Exception e) {
            model.addAttribute("orders", Collections.emptyList());
        }
        
        try {
            List<MenuItem> items = menuItemRepository.findAll();
            model.addAttribute("menuItems", items != null ? items : Collections.emptyList());
        } catch (Exception e) {
            model.addAttribute("menuItems", Collections.emptyList());
        }
        return "admin/orders";
    }
    
    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam String status, RedirectAttributes redirectAttributes) {
        try {
            Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
            order.setStatus(Order.OrderStatus.valueOf(status));
            orderRepository.save(order);
            redirectAttributes.addFlashAttribute("successMessage", "Order status updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating order: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }
    
    // Review Management
    @GetMapping("/reviews")
    public String manageReviews(Model model) {
        try {
            List<Review> allReviews = reviewRepository.findAll();
            System.out.println("DEBUG: Admin - Retrieved " + (allReviews != null ? allReviews.size() : 0) + " reviews from database");
            
            if (allReviews != null) {
                for (Review review : allReviews) {
                    System.out.println("DEBUG: Review ID: " + review.getId() + ", Name: " + review.getCustomerName() + ", Approved: " + review.isApproved());
                }
            }
            model.addAttribute("reviews", allReviews != null ? allReviews : Collections.emptyList());
        } catch (Exception e) {
            System.out.println("DEBUG: Error fetching reviews: " + e.getMessage());
            model.addAttribute("reviews", Collections.emptyList());
        }
        return "admin/reviews";
    }

    
    @PostMapping("/reviews/{id}/approve")
    public String approveReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Review review = reviewRepository.findById(id).orElseThrow(() -> new RuntimeException("Review not found"));
            review.setApproved(true);
            reviewRepository.save(review);
            redirectAttributes.addFlashAttribute("successMessage", "Review approved!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error approving review: " + e.getMessage());
        }
        return "redirect:/admin/reviews";
    }
    
    @PostMapping("/reviews/{id}/delete")
    public String deleteReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reviewRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Review deleted!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting review: " + e.getMessage());
        }
        return "redirect:/admin/reviews";
    }
    
    // User Management
    @GetMapping("/users")
    public String manageUsers(Model model) {
        try {
            List<User> users = userRepository.findAll();
            model.addAttribute("users", users != null ? users : Collections.emptyList());
        } catch (Exception e) {
            model.addAttribute("users", Collections.emptyList());
        }
        return "admin/users";
    }
    
    @PostMapping("/users/save")
    public String saveUser(@ModelAttribute User user, @RequestParam String password, RedirectAttributes redirectAttributes) {
        try {
            if (user.getId() == null || (password != null && !password.isEmpty())) {
                if (password == null || password.isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Password is required for new users.");
                    return "redirect:/admin/users";
                }
                user.setPassword(passwordEncoder.encode(password));
            } else {
                User existing = userRepository.findById(user.getId()).orElse(user);
                user.setPassword(existing.getPassword());
            }
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("successMessage", "User saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
    
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
    
    // Create Offline Order (for POS/Billing)
    @PostMapping("/orders/create-offline")
    public String createOfflineOrder(@RequestParam String customerName,
                                    @RequestParam String phone,
                                    @RequestParam String items,
                                    @RequestParam String totalAmount,
                                    @RequestParam(defaultValue = "") String address,
                                    @RequestParam(defaultValue = "") String deliveryNotes,
                                    @RequestParam(defaultValue = "CASH") String paymentType,
                                    RedirectAttributes redirectAttributes) {
        try {
            Order order = new Order();
            order.setCustomerName(customerName);
            order.setPhone(phone);
            order.setItems(items);
            order.setTotalAmount(new BigDecimal(totalAmount));
            order.setAddress(address);
            order.setDeliveryNotes(deliveryNotes);
            order.setOrderType("OFFLINE");
            order.setPaymentStatus("PAID");
            order.setPaymentType(paymentType);
            order.setStatus(Order.OrderStatus.DELIVERED);
            
            Order savedOrder = orderRepository.save(order);
            return "redirect:/admin/orders?printBill=" + savedOrder.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating order: " + e.getMessage());
            return "redirect:/admin/orders";
        }
    }
    
    // Print Bill
    @GetMapping("/orders/{id}/print")
    public String printBill(@PathVariable Long id, Model model) {
        try {
            Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
            model.addAttribute("order", order);
            
            String billMessage = "Thank you for your order from GM Caffe!\n\n" +
                "Bill ID: " + (order.getBillId() != null ? order.getBillId() : "N/A") + "\n" +
                "Items: " + (order.getItems() != null ? order.getItems() : "N/A") + "\n" +
                "Total: ₹" + (order.getTotalAmount() != null ? order.getTotalAmount() : "0") + "\n" +
                "Payment Status: " + (order.getPaymentStatus() != null ? order.getPaymentStatus() : "N/A") + "\n\n" +
                "Thank you for visiting GM Caffe!";
            model.addAttribute("billMessage", billMessage);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading bill: " + e.getMessage());
        }
        return "admin/bill";
    }
    
    // Send Bill via WhatsApp (Text Message)
    @GetMapping("/orders/{id}/whatsapp")
    public String sendToWhatsApp(@PathVariable Long id, Model model) {
        try {
            Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
            
            String phone = order.getPhone() != null ? order.getPhone().replaceAll("\\D", "") : "";
            if (phone.length() == 10) {
                phone = "91" + phone;
            }
            
            String addressLine = "";
            if (order.getAddress() != null && !order.getAddress().trim().isEmpty()) {
                addressLine = "*Address:* " + order.getAddress() + "\n\n";
            }
            
            String billMessage = "🧾 *GM Caffe - Bill Receipt*\n\n" +
                "━━━━━━━━━━━━━━━━━━\n\n" +
                "*Bill ID:* " + (order.getBillId() != null ? order.getBillId() : "N/A") + "\n" +
                "*Date:* " + (order.getOrderedAt() != null ? order.getOrderedAt().toLocalDate().toString() : "N/A") + "\n" +
                "*Customer:* " + (order.getCustomerName() != null ? order.getCustomerName() : "N/A") + "\n" +
                addressLine +
                "━━━━━━━━━━━━━━━━━━\n\n" +
                "*Order Details:*\n" + (order.getItems() != null ? order.getItems() : "N/A") + "\n\n" +
                "━━━━━━━━━━━━━━━━━━\n\n" +
                "*Total Amount:* ₹" + (order.getTotalAmount() != null ? order.getTotalAmount() : "0") + "\n" +
                "*Payment Status:* " + (order.getPaymentStatus() != null ? order.getPaymentStatus() : "N/A") + "\n\n" +
                "━━━━━━━━━━━━━━━━━━\n\n" +
                "Thank you for visiting *GM Caffe*! ☕\n" +
                "Please visit again!";
            
            return "redirect:https://wa.me/" + phone + "?text=" + java.net.URLEncoder.encode(billMessage);
        } catch (Exception e) {
            return "redirect:/admin/orders?error=true";
        }
    }
    
    // Download Bill as Text
    @GetMapping("/orders/{id}/pdf")
    public void downloadPdf(@PathVariable Long id, jakarta.servlet.http.HttpServletResponse response) {
        try {
            Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
            PdfService.generateBillPdf(order, response);
        } catch (Exception e) {
            try {
                response.setContentType("text/plain");
                response.getWriter().write("Error generating bill: " + e.getMessage());
            } catch (IOException ex) {
                System.err.println("Error writing response: " + ex.getMessage());
            }
        }
    }
    
    // Update Payment Status
    @PostMapping("/orders/{id}/payment")
    public String updatePaymentStatus(@PathVariable Long id, @RequestParam String paymentStatus, RedirectAttributes redirectAttributes) {
        try {
            Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
            order.setPaymentStatus(paymentStatus);
            orderRepository.save(order);
            redirectAttributes.addFlashAttribute("successMessage", "Payment status updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating payment: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }
    
    // Delete Order
    @GetMapping("/orders/delete/{id}")
    public String deleteOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Order deleted!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting order: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }
    
    // Financial Reports
    @GetMapping("/reports")
    public String reports(
            @RequestParam(required = false, defaultValue = "daily") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model) {
        
        java.time.LocalDateTime startDateTime;
        java.time.LocalDateTime endDateTime;
        java.time.LocalDate today = java.time.LocalDate.now();
        
        switch (period) {
            case "weekly":
                startDateTime = today.minusDays(7).atStartOfDay();
                endDateTime = today.atTime(23, 59, 59);
                model.addAttribute("periodLabel", "Last 7 Days");
                break;
            case "monthly":
                startDateTime = today.minusMonths(1).atStartOfDay();
                endDateTime = today.atTime(23, 59, 59);
                model.addAttribute("periodLabel", "Last 30 Days");
                break;
            case "yearly":
                startDateTime = today.minusYears(1).atStartOfDay();
                endDateTime = today.atTime(23, 59, 59);
                model.addAttribute("periodLabel", "Last Year");
                break;
            case "custom":
                if (startDate != null && endDate != null) {
                    startDateTime = java.time.LocalDate.parse(startDate).atStartOfDay();
                    endDateTime = java.time.LocalDate.parse(endDate).atTime(23, 59, 59);
                    model.addAttribute("periodLabel", "Custom Range");
                } else {
                    startDateTime = today.atStartOfDay();
                    endDateTime = today.atTime(23, 59, 59);
                    model.addAttribute("periodLabel", "Today");
                }
                break;
            default:
                startDateTime = today.atStartOfDay();
                endDateTime = today.atTime(23, 59, 59);
                model.addAttribute("periodLabel", "Today");
        }
        
        List<Order> orders;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalOrders = 0;
        BigDecimal avgOrderValue = BigDecimal.ZERO;
        
        try {
            orders = orderRepository.findByOrderedAtBetween(startDateTime, endDateTime);
            if (orders != null) {
                totalOrders = orders.size();
                totalRevenue = orderRepository.getTotalRevenueBetween(startDateTime, endDateTime);
                if (totalRevenue == null) {
                    totalRevenue = BigDecimal.ZERO;
                }
                if (totalOrders > 0) {
                    avgOrderValue = totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
                }
            } else {
                orders = Collections.emptyList();
            }
        } catch (Exception e) {
            System.err.println("Error fetching report data: " + e.getMessage());
            orders = Collections.emptyList();
        }
        
        long paidCount = 0;
        if (orders != null) {
            paidCount = orders.stream().filter(o -> "PAID".equals(o.getPaymentStatus())).count();
        }
        long paidPercentage = totalOrders > 0 ? (paidCount * 100) / totalOrders : 0;
        
        model.addAttribute("period", period);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("orders", orders);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("averageOrderValue", avgOrderValue);
        model.addAttribute("paidPercentage", paidPercentage);
        
        return "admin/reports";
    }
    
    // Export Report
    @GetMapping("/reports/export/pdf")
    public void exportPdf(
            @RequestParam(required = false, defaultValue = "daily") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            jakarta.servlet.http.HttpServletResponse response) throws Exception {
        
        List<Order> orders = getOrdersForPeriod(period, startDate, endDate);
        BigDecimal totalRevenue = orders.stream()
            .map(Order::getTotalAmount)
            .filter(amount -> amount != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        String label = getPeriodLabel(period, startDate, endDate);
        PdfService.generateReportPdf(orders, totalRevenue, label, response);
    }
    
    private String getPeriodLabel(String period, String startDate, String endDate) {
        java.time.LocalDate today = java.time.LocalDate.now();
        
        switch (period) {
            case "weekly": return "Last 7 Days";
            case "monthly": return "Last 30 Days";
            case "yearly": return "Last Year";
            case "custom": 
                if (startDate != null && endDate != null) {
                    return startDate + " to " + endDate;
                }
                return "Today";
            default: return "Today";
        }
    }
    
    // Export Excel
    @GetMapping("/reports/export/excel")
    public void exportExcel(
            @RequestParam(required = false, defaultValue = "daily") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            jakarta.servlet.http.HttpServletResponse response) throws Exception {
        
        List<Order> orders = getOrdersForPeriod(period, startDate, endDate);
        
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=GM_Caffe_Report.xls");
        
        StringBuilder content = new StringBuilder();
        content.append("Bill ID\tDate\tCustomer\tPhone\tOrder Type\tItems\tTotal Amount\tPayment Status\tStatus\n");
        
        if (orders != null) {
            for (Order order : orders) {
                content.append(safeString(order.getBillId())).append("\t")
                       .append(order.getOrderedAt() != null ? order.getOrderedAt().toString() : "").append("\t")
                       .append(safeString(order.getCustomerName())).append("\t")
                       .append(safeString(order.getPhone())).append("\t")
                       .append(safeString(order.getOrderType())).append("\t")
                       .append(safeString(order.getItems()).replace("\n", " ")).append("\t")
                       .append(order.getTotalAmount() != null ? order.getTotalAmount().toString() : "0").append("\t")
                       .append(safeString(order.getPaymentStatus())).append("\t")
                       .append(order.getStatus() != null ? order.getStatus().name() : "").append("\n");
            }
        }
        
        response.getWriter().write(content.toString());
    }
    
    // Export CSV
    @GetMapping("/reports/export/csv")
    public void exportCsv(
            @RequestParam(required = false, defaultValue = "daily") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            jakarta.servlet.http.HttpServletResponse response) throws Exception {
        
        List<Order> orders = getOrdersForPeriod(period, startDate, endDate);
        
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=GM_Caffe_Report.csv");
        
        StringBuilder content = new StringBuilder();
        content.append("Bill ID,Date,Customer,Phone,Order Type,Items,Total Amount,Payment Status,Status\n");
        
        if (orders != null) {
            for (Order order : orders) {
                content.append(safeString(order.getBillId())).append(",")
                       .append(order.getOrderedAt() != null ? order.getOrderedAt().toString() : "").append(",")
                       .append("\"").append(safeString(order.getCustomerName())).append("\",")
                       .append(safeString(order.getPhone())).append(",")
                       .append(safeString(order.getOrderType())).append(",")
                       .append("\"").append(safeString(order.getItems()).replace("\n", " ")).append("\",")
                       .append(order.getTotalAmount() != null ? order.getTotalAmount().toString() : "0").append(",")
                       .append(safeString(order.getPaymentStatus())).append(",")
                       .append(order.getStatus() != null ? order.getStatus().name() : "").append("\n");
            }
        }
        
        response.getWriter().write(content.toString());
    }
    
    // Export Word
    @GetMapping("/reports/export/word")
    public void exportWord(
            @RequestParam(required = false, defaultValue = "daily") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            jakarta.servlet.http.HttpServletResponse response) throws Exception {
        
        List<Order> orders = getOrdersForPeriod(period, startDate, endDate);
        
        BigDecimal totalRevenue = BigDecimal.ZERO;
        if (orders != null && !orders.isEmpty()) {
            totalRevenue = orders.stream()
                .map(Order::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        
        String periodLabel = getPeriodLabel(period, startDate, endDate);
        
        // Create Word-compatible HTML document
        StringBuilder content = new StringBuilder();
        content.append("<html xmlns:o='urn:schemas-microsoft-com:office:office' ");
        content.append("xmlns:w='urn:schemas-microsoft-com:office:word' ");
        content.append("xmlns='http://www.w3.org/TR/REC-html40'>");
        content.append("<head><meta charset='utf-8'><title>GM Caffe Report</title></head><body>");
        
        content.append("<h1 style='text-align:center;'>GM Caffe - Financial Report</h1>");
        content.append("<p style='text-align:center;'><strong>Period:</strong> ").append(periodLabel).append("</p>");
        
        content.append("<h2>Summary</h2>");
        content.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse:collapse;'>");
        content.append("<tr><td><strong>Total Orders</strong></td><td>").append(orders != null ? orders.size() : 0).append("</td></tr>");
        content.append("<tr><td><strong>Total Revenue</strong></td><td>Rs. ").append(totalRevenue).append("</td></tr>");
        
        if (orders != null && !orders.isEmpty() && totalRevenue != null) {
            double avgOrderValue = totalRevenue.doubleValue() / orders.size();
            content.append("<tr><td><strong>Average Order Value</strong></td><td>Rs. ").append(String.format("%.2f", avgOrderValue)).append("</td></tr>");
        }
        
        long paidCount = orders != null ? orders.stream().filter(o -> "PAID".equals(o.getPaymentStatus())).count() : 0;
        content.append("<tr><td><strong>Paid Orders</strong></td><td>").append(paidCount).append("</td></tr>");
        content.append("</table>");
        
        content.append("<h2>Order Details</h2>");
        content.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse:collapse;'>");
        content.append("<tr style='background-color:#f2f2f2;'>");
        content.append("<th>Bill ID</th><th>Date</th><th>Customer</th><th>Phone</th>");
        content.append("<th>Items</th><th>Amount</th><th>Payment</th><th>Status</th>");
        content.append("</tr>");
        
        if (orders != null) {
            for (Order order : orders) {
                content.append("<tr>");
                content.append("<td>").append(safeString(order.getBillId())).append("</td>");
                content.append("<td>").append(order.getOrderedAt() != null ? order.getOrderedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) : "").append("</td>");
                content.append("<td>").append(safeString(order.getCustomerName())).append("</td>");
                content.append("<td>").append(safeString(order.getPhone())).append("</td>");
                content.append("<td>").append(safeString(order.getItems()).replace("\n", ", ")).append("</td>");
                content.append("<td>Rs.").append(order.getTotalAmount() != null ? order.getTotalAmount() : "0").append("</td>");
                content.append("<td>").append(safeString(order.getPaymentStatus())).append("</td>");
                content.append("<td>").append(order.getStatus() != null ? order.getStatus().name() : "").append("</td>");
                content.append("</tr>");
            }
        }
        
        content.append("</table>");
        content.append("<p style='text-align:center;margin-top:20px;'>Generated by GM Caffe Admin</p>");
        content.append("</body></html>");
        
        response.setContentType("application/msword");
        response.setHeader("Content-Disposition", "attachment; filename=GM_Caffe_Report.doc");
        response.getWriter().write(content.toString());
    }
    
    private List<Order> getOrdersForPeriod(String period, String startDate, String endDate) {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDateTime startDateTime;
        java.time.LocalDateTime endDateTime;
        
        switch (period) {
            case "weekly":
                startDateTime = today.minusDays(7).atStartOfDay();
                endDateTime = today.atTime(23, 59, 59);
                break;
            case "monthly":
                startDateTime = today.minusMonths(1).atStartOfDay();
                endDateTime = today.atTime(23, 59, 59);
                break;
            case "yearly":
                startDateTime = today.minusYears(1).atStartOfDay();
                endDateTime = today.atTime(23, 59, 59);
                break;
            case "custom":
                if (startDate != null && endDate != null) {
                    startDateTime = java.time.LocalDate.parse(startDate).atStartOfDay();
                    endDateTime = java.time.LocalDate.parse(endDate).atTime(23, 59, 59);
                } else {
                    startDateTime = today.atStartOfDay();
                    endDateTime = today.atTime(23, 59, 59);
                }
                break;
            default:
                startDateTime = today.atStartOfDay();
                endDateTime = today.atTime(23, 59, 59);
        }
        
        try {
            return orderRepository.findByOrderedAtBetween(startDateTime, endDateTime);
        } catch (Exception e) {
            System.err.println("Error fetching orders: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    // ==================== OFFER MANAGEMENT ====================
    
    // Get all offers
    @GetMapping("/offers")
    public String manageOffers(Model model) {
        try {
            List<Offer> offers = offerRepository.findAllByOrderByDisplayOrderAsc();
            model.addAttribute("offers", offers != null ? offers : Collections.emptyList());
        } catch (Exception e) {
            model.addAttribute("offers", Collections.emptyList());
        }
        model.addAttribute("offer", new Offer());
        return "admin/offers";
    }
    
    // Save offer (with image upload)
    @PostMapping("/offers/save")
    public String saveOffer(@ModelAttribute Offer offer,
                           @RequestParam(value = "imageFile", required = false) MultipartFile file,
                           RedirectAttributes redirectAttributes) {
        try {
            if (file != null && !file.isEmpty()) {
                String uploadDir = servletContext.getRealPath("/uploads");
                if (uploadDir == null) {
                    uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/uploads";
                }
                
                File uploadDirFile = new File(uploadDir);
                if (!uploadDirFile.exists()) {
                    uploadDirFile.mkdirs();
                }
                
                String originalFilename = file.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String newFilename = UUID.randomUUID().toString() + extension;
                
                Path filePath = Paths.get(uploadDir, newFilename);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                
                offer.setImageUrl("/uploads/" + newFilename);
            }
            
            // Handle existing image URL for edits
            if (offer.getId() != null && offer.getImageUrl() == null) {
                Offer existing = offerRepository.findById(offer.getId()).orElse(offer);
                offer.setImageUrl(existing.getImageUrl());
            }
            
            offerRepository.save(offer);
            redirectAttributes.addFlashAttribute("successMessage", "Offer saved successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving offer: " + e.getMessage());
        }
        
        return "redirect:/admin/offers";
    }
    
    // Edit offer
    @GetMapping("/offers/edit/{id}")
    public String editOffer(@PathVariable Long id, Model model) {
        try {
            Offer offer = offerRepository.findById(id).orElse(new Offer());
            model.addAttribute("offer", offer);
        } catch (Exception e) {
            model.addAttribute("offer", new Offer());
        }
        
        try {
            List<Offer> offers = offerRepository.findAllByOrderByDisplayOrderAsc();
            model.addAttribute("offers", offers != null ? offers : Collections.emptyList());
        } catch (Exception e) {
            model.addAttribute("offers", Collections.emptyList());
        }
        return "admin/offers";
    }
    
    // Delete offer
    @GetMapping("/offers/delete/{id}")
    public String deleteOffer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            offerRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Offer deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting offer: " + e.getMessage());
        }
        return "redirect:/admin/offers";
    }
    
    // Toggle offer active status
    @PostMapping("/offers/{id}/toggle")
    public String toggleOfferStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Offer offer = offerRepository.findById(id).orElseThrow(() -> new RuntimeException("Offer not found"));
            offer.setActive(!offer.isActive());
            offerRepository.save(offer);
            redirectAttributes.addFlashAttribute("successMessage", "Offer status updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating offer: " + e.getMessage());
        }
        return "redirect:/admin/offers";
    }
    
    // ==================== GALLERY MANAGEMENT ====================
    
    // Get all gallery items
    @GetMapping("/gallery")
    public String manageGallery(Model model) {
        try {
            List<Gallery> galleryItems = galleryRepository.findAllByOrderByDisplayOrderAsc();
            model.addAttribute("galleryItems", galleryItems != null ? galleryItems : Collections.emptyList());
        } catch (Exception e) {
            model.addAttribute("galleryItems", Collections.emptyList());
        }
        model.addAttribute("gallery", new Gallery());
        return "admin/gallery";
    }
    
    // Save gallery (with image upload)
    @PostMapping("/gallery/save")
    public String saveGallery(@ModelAttribute Gallery gallery,
                            @RequestParam(value = "imageFile", required = false) MultipartFile file,
                            RedirectAttributes redirectAttributes) {
        try {
            if (file != null && !file.isEmpty()) {
                String uploadDir = servletContext.getRealPath("/uploads");
                if (uploadDir == null) {
                    uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/uploads";
                }
                
                File uploadDirFile = new File(uploadDir);
                if (!uploadDirFile.exists()) {
                    uploadDirFile.mkdirs();
                }
                
                String originalFilename = file.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String newFilename = UUID.randomUUID().toString() + extension;
                
                Path filePath = Paths.get(uploadDir, newFilename);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                
                gallery.setImageUrl("/uploads/" + newFilename);
            }
            
            // Handle existing image URL for edits
            if (gallery.getId() != null && gallery.getImageUrl() == null) {
                Gallery existing = galleryRepository.findById(gallery.getId()).orElse(gallery);
                gallery.setImageUrl(existing.getImageUrl());
            }
            
            galleryRepository.save(gallery);
            redirectAttributes.addFlashAttribute("successMessage", "Gallery photo saved successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving gallery photo: " + e.getMessage());
        }
        
        return "redirect:/admin/gallery";
    }
    
    // Edit gallery
    @GetMapping("/gallery/edit/{id}")
    public String editGallery(@PathVariable Long id, Model model) {
        try {
            Gallery gallery = galleryRepository.findById(id).orElse(new Gallery());
            model.addAttribute("gallery", gallery);
        } catch (Exception e) {
            model.addAttribute("gallery", new Gallery());
        }
        
        try {
            List<Gallery> galleryItems = galleryRepository.findAllByOrderByDisplayOrderAsc();
            model.addAttribute("galleryItems", galleryItems != null ? galleryItems : Collections.emptyList());
        } catch (Exception e) {
            model.addAttribute("galleryItems", Collections.emptyList());
        }
        return "admin/gallery";
    }
    
    // Delete gallery
    @GetMapping("/gallery/delete/{id}")
    public String deleteGallery(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            galleryRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Gallery photo deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting gallery photo: " + e.getMessage());
        }
        return "redirect:/admin/gallery";
    }
    
    // Helper method for null-safe string
    private String safeString(String value) {
        return value != null ? value : "";
    }
}

