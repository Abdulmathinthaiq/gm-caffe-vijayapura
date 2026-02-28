package com.gmcaffe.repositories;

import com.gmcaffe.models.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    
    List<MenuItem> findByIsActiveTrueOrderByNameAsc();
    
    List<MenuItem> findByCategoryAndIsActiveTrue(String category);
    
    List<MenuItem> findByIsFeaturedTrueAndIsActiveTrue();
    
    @Query("SELECT DISTINCT m.category FROM MenuItem m WHERE m.isActive = true")
    List<String> findAllCategories();
}
