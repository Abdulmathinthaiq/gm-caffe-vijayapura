package com.gmcaffe.config.repositories;

import com.gmcaffe.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository1 extends JpaRepository<Order, Long> {
    
    List<Order> findByStatus(Order.OrderStatus status);
    
    List<Order> findByCustomerName(String customerName);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(Order.OrderStatus status);
    
    @Query("SELECT COUNT(o) FROM Order o")
    long countTotal();
    
    List<Order> findAllByOrderByOrderedAtDesc();
}
