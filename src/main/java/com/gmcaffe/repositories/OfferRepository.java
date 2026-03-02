package com.gmcaffe.repositories;

import com.gmcaffe.models.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    
    List<Offer> findByActiveTrueOrderByDisplayOrderAsc();
    
    List<Offer> findAllByOrderByDisplayOrderAsc();
}
