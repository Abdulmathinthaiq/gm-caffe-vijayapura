package com.gmcaffe.repositories;

import com.gmcaffe.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByStatusOrderByOrderedAtDesc(Order.OrderStatus status);

    
    List<Order> findByCustomerNameContainingIgnoreCase(String customerName);
    
    List<Order> findByPhoneContaining(String phone);
    
    long countByStatus(Order.OrderStatus status);
    
    @Query("SELECT COUNT(o) FROM Order o")
    long countTotal();
    
    List<Order> findAllByOrderByOrderedAtDesc();
    
    // Financial Report Queries
    List<Order> findByOrderedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderedAt BETWEEN :startDate AND :endDate")
    java.math.BigDecimal getTotalRevenueBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderedAt BETWEEN :startDate AND :endDate")
    long countOrdersBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT o.paymentStatus, COUNT(o) FROM Order o WHERE o.orderedAt BETWEEN :startDate AND :endDate GROUP BY o.paymentStatus")
    List<Object[]> getPaymentStatusCountsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT o.orderType, COUNT(o) FROM Order o WHERE o.orderedAt BETWEEN :startDate AND :endDate GROUP BY o.orderType")
    List<Object[]> getOrderTypeCountsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

}
