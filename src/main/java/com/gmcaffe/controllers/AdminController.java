package com.gmcaffe.controllers;

import com.gmcaffe.repositories.MenuItemRepository;
import com.gmcaffe.repositories.OfferRepository;
import com.gmcaffe.repositories.OrderRepository;
import com.gmcaffe.repositories.ReviewRepository;
import com.gmcaffe.repositories.UserRepository;

import com.gmcaffe.models.MenuItem;
import com.gmcaffe.models.Offer;
import com.gmcaffe.models.Order;
import com.gmcaffe.models.Review;
import com.gmcaffe.models.User;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    private ServletContext servletContext;
    
    public AdminController(MenuItemRepository menuItemRepository,
                          OrderRepository orderRepository,
                          ReviewRepository reviewRepository,
                          UserRepository userRepository,
                          OfferRepository offerRepository,
                          PasswordEncoder passwordEncoder) {

        this.menuItemRepository = menuItemRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.offerRepository = offerRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @GetMapping("/login")
    public String login() {
        return "admin/login";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalOrders", orderRepository.countTotal());
        model.addAttribute("pendingOrders", orderRepository.countByStatus(Order.OrderStatus.PENDING));
        model.addAttribute("totalMenuItems", menuItemRepository.count());
        model.addAttribute("newReviews", reviewRepository.countByApprovedFalse());
        
        Double avgRating = reviewRepository.getAverageRating();
        model.addAttribute("avgRating", avgRating != null ? avgRating : 0);
        
        return "admin/dashboard";
    }
    
    // Menu Management
    @GetMapping("/menu")
    public String manageMenu(Model model) {
        model.addAttribute("items", menuItemRepository.findAll());
        model.addAttribute("item", new MenuItem());
        return "admin/menu";
    }
    
    @PostMapping("/menu/save")
    public String saveMenu(@ModelAttribute MenuItem item) {
        if (item.getId() != null) {
            MenuItem existing = menuItemRepository.findById(item.getId()).orElse(item);
            item.setImageUrl(existing.getImageUrl());
        }
        menuItemRepository.save(item);
        return "redirect:/admin/menu";
    }
    
    @GetMapping("/menu/edit/{id}")
    public String editMenu(@PathVariable Long id, Model model) {
        MenuItem item = menuItemRepository.findById(id).orElse(new MenuItem());
        model.addAttribute("item", item);
        model.addAttribute("items", menuItemRepository.findAll());
        return "admin/menu";
    }
    
    @GetMapping("/menu/delete/{id}")
    public String deleteMenu(@PathVariable Long id) {
        menuItemRepository.deleteById(id);
        return "redirect:/admin/menu";
    }
    
    // Order Management
    @GetMapping("/orders")
    public String manageOrders(Model model) {
        model.addAttribute("orders", orderRepository.findAllByOrderByOrderedAtDesc());
        model.addAttribute("menuItems", menuItemRepository.findAll());
        return "admin/orders";
    }
    
    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        Order order = orderRepository.findById(id).orElseThrow();
        order.setStatus(Order.OrderStatus.valueOf(status));
        orderRepository.save(order);
        return "redirect:/admin/orders";
    }
    
    // Review Management
    @GetMapping("/reviews")
    public String manageReviews(Model model) {
        List<Review> allReviews = reviewRepository.findAll();
        System.out.println("DEBUG: Admin - Retrieved " + allReviews.size() + " reviews from database");
        for (Review review : allReviews) {
            System.out.println("DEBUG: Review ID: " + review.getId() + ", Name: " + review.getCustomerName() + ", Approved: " + review.isApproved());
        }
        model.addAttribute("reviews", allReviews);
        return "admin/reviews";
    }

    
    @PostMapping("/reviews/{id}/approve")
    public String approveReview(@PathVariable Long id) {
        Review review = reviewRepository.findById(id).orElseThrow();
        review.setApproved(true);
        reviewRepository.save(review);
        return "redirect:/admin/reviews";
    }
    
    @PostMapping("/reviews/{id}/delete")
    public String deleteReview(@PathVariable Long id) {
        reviewRepository.deleteById(id);
        return "redirect:/admin/reviews";
    }
    
    // User Management
    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }
    
    @PostMapping("/users/save")
    public String saveUser(@ModelAttribute User user, @RequestParam String password) {
        if (user.getId() == null || password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        } else {
            User existing = userRepository.findById(user.getId()).orElse(user);
            user.setPassword(existing.getPassword());
        }
        userRepository.save(user);
        return "redirect:/admin/users";
    }
    
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin/users";
    }
    
    // Create Offline Order (for POS/Billing)
    @PostMapping("/orders/create-offline")
    public String createOfflineOrder(@RequestParam String customerName,
                                    @RequestParam String phone,
                                    @RequestParam String items,
                                    @RequestParam String totalAmount,
                                    @RequestParam(defaultValue = "") String address,
                                    @RequestParam(defaultValue = "") String deliveryNotes) {
        Order order = new Order();
        order.setCustomerName(customerName);
        order.setPhone(phone);
        order.setItems(items);
        order.setTotalAmount(new java.math.BigDecimal(totalAmount));
        order.setAddress(address);
        order.setDeliveryNotes(deliveryNotes);
        order.setOrderType("OFFLINE");
        order.setPaymentStatus("PAID");
        order.setStatus(Order.OrderStatus.DELIVERED);
        
        orderRepository.save(order);
        return "redirect:/admin/orders?printBill=" + order.getId();
    }
    
    // Print Bill
    @GetMapping("/orders/{id}/print")
    public String printBill(@PathVariable Long id, Model model) {
        Order order = orderRepository.findById(id).orElseThrow();
        model.addAttribute("order", order);
        
        String billMessage = "Thank you for your order from GM Caffe!\n\n" +
            "Bill ID: " + order.getBillId() + "\n" +
            "Items: " + order.getItems() + "\n" +
            "Total: ₹" + order.getTotalAmount() + "\n" +
            "Payment Status: " + order.getPaymentStatus() + "\n\n" +
            "Thank you for visiting GM Caffe!";
        model.addAttribute("billMessage", billMessage);
        
        return "admin/bill";
    }
    
    // Send Bill via WhatsApp (Text Message)
    @GetMapping("/orders/{id}/whatsapp")
    public String sendToWhatsApp(@PathVariable Long id, Model model) {
        Order order = orderRepository.findById(id).orElseThrow();
        
        String phone = order.getPhone().replaceAll("\\D", "");
        if (phone.length() == 10) {
            phone = "91" + phone;
        }
        
        // Build address line if available
        String addressLine = "";
        if (order.getAddress() != null && !order.getAddress().trim().isEmpty()) {
            addressLine = "*Address:* " + order.getAddress() + "\n\n";
        }
        
        String billMessage = "🧾 *GM Caffe - Bill Receipt*\n\n" +
            "━━━━━━━━━━━━━━━━━━\n\n" +
            "*Bill ID:* " + order.getBillId() + "\n" +
            "*Date:* " + order.getOrderedAt().toLocalDate() + "\n" +
            "*Customer:* " + order.getCustomerName() + "\n" +
            addressLine +
            "━━━━━━━━━━━━━━━━━━\n\n" +
            "*Order Details:*\n" + order.getItems() + "\n\n" +
            "━━━━━━━━━━━━━━━━━━\n\n" +
            "*Total Amount:* ₹" + order.getTotalAmount() + "\n" +
            "*Payment Status:* " + order.getPaymentStatus() + "\n\n" +
            "━━━━━━━━━━━━━━━━━━\n\n" +
            "Thank you for visiting *GM Caffe*! ☕\n" +
            "Please visit again!";
        
        return "redirect:https://wa.me/" + phone + "?text=" + java.net.URLEncoder.encode(billMessage);
    }
    
    // Send Bill via WhatsApp with PDF
    @GetMapping("/orders/{id}/whatsapp-pdf")
    public String sendToWhatsAppPdf(@PathVariable Long id, jakarta.servlet.http.HttpServletResponse response) throws Exception {
        Order order = orderRepository.findById(id).orElseThrow();
        
        String phone = order.getPhone().replaceAll("\\D", "");
        if (phone.length() == 10) {
            phone = "91" + phone;
        }
        
        // Build address line if available
        String addressLine = "";
        if (order.getAddress() != null && !order.getAddress().trim().isEmpty()) {
            addressLine = "*Address:* " + order.getAddress() + "\n\n";
        }
        
        String billMessage = "🧾 *GM Caffe - Bill Receipt*\n\n" +
            "━━━━━━━━━━━━━━━━━━\n\n" +
            "*Bill ID:* " + order.getBillId() + "\n" +
            "*Date:* " + order.getOrderedAt().toLocalDate() + "\n" +
            "*Customer:* " + order.getCustomerName() + "\n" +
            addressLine +
            "━━━━━━━━━━━━━━━━━━\n\n" +
            "*Order Details:*\n" + order.getItems() + "\n\n" +
            "━━━━━━━━━━━━━━━━━━\n\n" +
            "*Total Amount:* ₹" + order.getTotalAmount() + "\n" +
            "*Payment Status:* " + order.getPaymentStatus() + "\n\n" +
            "━━━━━━━━━━━━━━━━━━\n\n" +
            "Thank you for visiting *GM Caffe*! ☕\n" +
            "Please visit again!";
        
        // Generate PDF and redirect to WhatsApp
        // Note: For PDF sharing via WhatsApp, the PDF needs to be uploaded to a server
        // For now, we'll redirect to WhatsApp with the text message
        return "redirect:https://wa.me/" + phone + "?text=" + java.net.URLEncoder.encode(billMessage);
    }
    
    // Download Bill PDF
    @GetMapping("/orders/{id}/pdf")
    public void downloadPdf(@PathVariable Long id, jakarta.servlet.http.HttpServletResponse response) throws Exception {
        Order order = orderRepository.findById(id).orElseThrow();
        PdfService.generateBillPdf(order, response);
    }
    
    // Update Payment Status
    @PostMapping("/orders/{id}/payment")
    public String updatePaymentStatus(@PathVariable Long id, @RequestParam String paymentStatus) {
        Order order = orderRepository.findById(id).orElseThrow();
        order.setPaymentStatus(paymentStatus);
        orderRepository.save(order);
        return "redirect:/admin/orders";
    }
    
    // Delete Order
    @GetMapping("/orders/delete/{id}")
    public String deleteOrder(@PathVariable Long id) {
        orderRepository.deleteById(id);
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
        
        List<Order> orders = orderRepository.findByOrderedAtBetween(startDateTime, endDateTime);
        java.math.BigDecimal totalRevenue = orderRepository.getTotalRevenueBetween(startDateTime, endDateTime);
        long totalOrders = orders.size();
        java.math.BigDecimal avgOrderValue = totalOrders > 0 ? 
            totalRevenue.divide(java.math.BigDecimal.valueOf(totalOrders), 2, java.math.RoundingMode.HALF_UP) : 
            java.math.BigDecimal.ZERO;
        
        long paidCount = orders.stream().filter(o -> "PAID".equals(o.getPaymentStatus())).count();
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
    
    // Export PDF
    @GetMapping("/reports/export/pdf")
    public void exportPdf(
            @RequestParam(required = false, defaultValue = "daily") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "Today") String periodLabel,
            jakarta.servlet.http.HttpServletResponse response) throws Exception {
        
        List<Order> orders = getOrdersForPeriod(period, startDate, endDate);
        java.math.BigDecimal totalRevenue = orders.stream().map(Order::getTotalAmount).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        
        // Get period label for the report
        String label = getPeriodLabel(period, startDate, endDate);
        
        // Generate actual PDF using PdfService
        PdfService.generateReportPdf(orders, totalRevenue, label, response);
    }
    
    // Helper method to get period label
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
        
        for (Order order : orders) {
            content.append(order.getBillId()).append("\t").append(order.getOrderedAt()).append("\t")
                   .append(order.getCustomerName()).append("\t").append(order.getPhone()).append("\t")
                   .append(order.getOrderType()).append("\t").append(order.getItems().replaceAll("\n", " ")).append("\t")
                   .append(order.getTotalAmount()).append("\t").append(order.getPaymentStatus()).append("\t")
                   .append(order.getStatus()).append("\n");
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
        java.math.BigDecimal totalRevenue = orders.stream().map(Order::getTotalAmount).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        
        response.setContentType("application/msword");
        response.setHeader("Content-Disposition", "attachment; filename=GM_Caffe_Report.doc");
        
        StringBuilder content = new StringBuilder();
        content.append("GM CAFFE - FINANCIAL REPORT\n");
        content.append("=====================================\n\n");
        content.append("Total Orders: ").append(orders.size()).append("\n");
        content.append("Total Revenue: Rs.").append(totalRevenue).append("\n\n");
        content.append("ORDER DETAILS\n");
        content.append("=====================================\n");
        
        for (Order order : orders) {
            content.append("Bill ID: ").append(order.getBillId()).append("\n");
            content.append("Date: ").append(order.getOrderedAt()).append("\n");
            content.append("Customer: ").append(order.getCustomerName()).append("\n");
            content.append("Phone: ").append(order.getPhone()).append("\n");
            content.append("Amount: Rs.").append(order.getTotalAmount()).append("\n");
            content.append("Payment: ").append(order.getPaymentStatus()).append("\n");
            content.append("-----------------------------------\n");
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
        
        for (Order order : orders) {
            content.append(order.getBillId()).append(",").append(order.getOrderedAt()).append(",")
                   .append("\"").append(order.getCustomerName()).append("\",").append(order.getPhone()).append(",")
                   .append(order.getOrderType()).append(",").append("\"").append(order.getItems().replaceAll("\n", " ")).append("\",")
                   .append(order.getTotalAmount()).append(",").append(order.getPaymentStatus()).append(",")
                   .append(order.getStatus()).append("\n");
        }
        
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
        
        return orderRepository.findByOrderedAtBetween(startDateTime, endDateTime);
    }
    
    // ==================== OFFER MANAGEMENT ====================
    
    // Get all offers
    @GetMapping("/offers")
    public String manageOffers(Model model) {
        model.addAttribute("offers", offerRepository.findAllByOrderByDisplayOrderAsc());
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
                // Get the uploads directory path
                String uploadDir = servletContext.getRealPath("/uploads");
                if (uploadDir == null) {
                    uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/uploads";
                }
                
                File uploadDirFile = new File(uploadDir);
                if (!uploadDirFile.exists()) {
                    uploadDirFile.mkdirs();
                }
                
                // Generate unique filename
                String originalFilename = file.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String newFilename = UUID.randomUUID().toString() + extension;
                
                // Save file
                Path filePath = Paths.get(uploadDir, newFilename);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                
                // Set the image URL
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
        Offer offer = offerRepository.findById(id).orElse(new Offer());
        model.addAttribute("offer", offer);
        model.addAttribute("offers", offerRepository.findAllByOrderByDisplayOrderAsc());
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
        Offer offer = offerRepository.findById(id).orElseThrow();
        offer.setActive(!offer.isActive());
        offerRepository.save(offer);
        return "redirect:/admin/offers";
    }
}
