package com.gmcaffe.config.repositories;

import com.gmcaffe.models.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository1 extends JpaRepository<MenuItem, Long> {
    
    List<MenuItem> findByCategoryAndIsActiveTrue(String category);
    
    List<MenuItem> findByIsActiveTrueOrderByNameAsc();
    
    List<MenuItem> findByIsFeaturedTrueAndIsActiveTrue();
    
    @Query("SELECT DISTINCT m.category FROM MenuItem m WHERE m.isActive = true")
    List<String> findAllCategories();
    
    List<MenuItem> findByCategory(String category);
}
