package com.gmcaffe.config.repositories;

import com.gmcaffe.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository1 extends JpaRepository<Review, Long> {
    
    List<Review> findByIsApprovedTrueOrderByCreatedAtDesc();
    
    List<Review> findByIsApprovedFalse();
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.isApproved = false")
    long countByApprovedFalse();
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.isApproved = true")
    Double getAverageRating();
    
    List<Review> findByCustomerName(String customerName);
}
