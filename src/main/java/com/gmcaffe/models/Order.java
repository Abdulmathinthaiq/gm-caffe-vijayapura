package com.gmcaffe.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_name", length = 100)
    private String customerName;
    
    @Column(length = 15)
    private String phone;
    
    @Column(columnDefinition = "TEXT")
    private String items;
    
    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(columnDefinition = "TEXT")
    private String address;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;
    
    @Column(name = "ordered_at")
    private LocalDateTime orderedAt;
    
    @Column(name = "delivery_notes")
    private String deliveryNotes;
    
    @Column(name = "bill_id", unique = true, length = 20)
    private String billId;
    
    @Column(name = "order_type", length = 20)
    private String orderType = "ONLINE"; // ONLINE or OFFLINE
    
    @Column(name = "payment_status", length = 20)
    private String paymentStatus = "PENDING"; // PENDING, PAID
    
    public enum OrderStatus {
        PENDING, PREPARING, READY, DELIVERED, CANCELLED
    }
    
    // Constructors
    public Order() {
        this.orderedAt = LocalDateTime.now();
        this.billId = generateBillId();
    }
    
    public Order(String customerName, String phone, String items, BigDecimal totalAmount, String address) {
        this.customerName = customerName;
        this.phone = phone;
        this.items = items;
        this.totalAmount = totalAmount;
        this.address = address;
        this.orderedAt = LocalDateTime.now();
        this.billId = generateBillId();
    }
    
    // Generate unique bill ID
    private String generateBillId() {
        return "GMC" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getItems() { return items; }
    public void setItems(String items) { this.items = items; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    
    public LocalDateTime getOrderedAt() { return orderedAt; }
    public void setOrderedAt(LocalDateTime orderedAt) { this.orderedAt = orderedAt; }
    
    public String getDeliveryNotes() { return deliveryNotes; }
    public void setDeliveryNotes(String deliveryNotes) { this.deliveryNotes = deliveryNotes; }
    
    public String getBillId() { return billId; }
    public void setBillId(String billId) { this.billId = billId; }
    
    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }
    
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
}
