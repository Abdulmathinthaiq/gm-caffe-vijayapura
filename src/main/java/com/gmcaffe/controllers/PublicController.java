package com.gmcaffe.controllers;

import com.gmcaffe.repositories.MenuItemRepository;
import com.gmcaffe.repositories.OfferRepository;
import com.gmcaffe.repositories.OrderRepository;
import com.gmcaffe.repositories.ReviewRepository;
import com.gmcaffe.models.MenuItem;
import com.gmcaffe.models.Offer;
import com.gmcaffe.models.Order;
import com.gmcaffe.models.Review;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Controller
public class PublicController {
    
    private final MenuItemRepository menuItemRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final OfferRepository offerRepository;
    
    public PublicController(MenuItemRepository menuItemRepository, 
                           OrderRepository orderRepository,
                           ReviewRepository reviewRepository,
                           OfferRepository offerRepository) {
        this.menuItemRepository = menuItemRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.offerRepository = offerRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        // Get featured items with null safety
        List<MenuItem> featuredItems;
        try {
            featuredItems = menuItemRepository.findByIsFeaturedTrueAndIsActiveTrue();
            if (featuredItems == null || featuredItems.isEmpty()) {
                List<MenuItem> allItems = menuItemRepository.findByIsActiveTrueOrderByNameAsc();
                featuredItems = allItems != null ? allItems.stream().limit(6).toList() : Collections.emptyList();
            }
        } catch (Exception e) {
            System.err.println("Error fetching featured items: " + e.getMessage());
            featuredItems = Collections.emptyList();
        }
        model.addAttribute("featuredItems", featuredItems);
        
        // Get recent reviews with null safety
        List<Review> recentReviews;
        try {
            List<Review> allReviews = reviewRepository.findByIsApprovedTrueOrderByCreatedAtDesc();
            recentReviews = allReviews != null ? allReviews.stream().limit(5).toList() : Collections.emptyList();
        } catch (Exception e) {
            System.err.println("Error fetching reviews: " + e.getMessage());
            recentReviews = Collections.emptyList();
        }
        model.addAttribute("recentReviews", recentReviews);
        
        // Get average rating with null safety
        Double avgRating;
        try {
            avgRating = reviewRepository.getAverageRating();
        } catch (Exception e) {
            System.err.println("Error calculating average rating: " + e.getMessage());
            avgRating = 0.0;
        }
        model.addAttribute("avgRating", avgRating != null ? avgRating : 0.0);
        
        // Get active offers with null safety
        List<Offer> activeOffers;
        try {
            List<Offer> allOffers = offerRepository.findByActiveTrueOrderByDisplayOrderAsc();
            activeOffers = allOffers != null ? allOffers : Collections.emptyList();
        } catch (Exception e) {
            System.err.println("Error fetching offers: " + e.getMessage());
            activeOffers = Collections.emptyList();
        }
        model.addAttribute("offers", activeOffers);
        
        return "index";
    }
    
    @GetMapping("/menu")
    public String menu(Model model, @RequestParam(required = false) String category) {
        List<MenuItem> items;
        try {
            if (category != null && !category.isEmpty()) {
                items = menuItemRepository.findByCategoryAndIsActiveTrue(category);
            } else {
                items = menuItemRepository.findByIsActiveTrueOrderByNameAsc();
            }
        } catch (Exception e) {
            System.err.println("Error fetching menu items: " + e.getMessage());
            items = Collections.emptyList();
        }
        model.addAttribute("allItems", items != null ? items : Collections.emptyList());
        
        List<String> categories;
        try {
            categories = menuItemRepository.findAllCategories();
        } catch (Exception e) {
            System.err.println("Error fetching categories: " + e.getMessage());
            categories = Collections.emptyList();
        }
        model.addAttribute("categories", categories != null ? categories : Collections.emptyList());
        model.addAttribute("selectedCategory", category);
        
        return "menu";
    }
    
    @GetMapping("/about")
    public String about() {
        return "about";
    }
    
    @GetMapping("/gallery")
    public String gallery() {
        return "gallery";
    }
    
    @GetMapping("/reviews")
    public String reviews(Model model) {
        List<Review> approvedReviews;
        try {
            approvedReviews = reviewRepository.findByIsApprovedTrueOrderByCreatedAtDesc();
        } catch (Exception e) {
            System.err.println("Error fetching reviews: " + e.getMessage());
            approvedReviews = Collections.emptyList();
        }
        model.addAttribute("reviews", approvedReviews != null ? approvedReviews : Collections.emptyList());
        
        Double avgRating;
        try {
            avgRating = reviewRepository.getAverageRating();
        } catch (Exception e) {
            System.err.println("Error calculating average rating: " + e.getMessage());
            avgRating = 0.0;
        }
        model.addAttribute("avgRating", avgRating != null ? avgRating : 0.0);
        
        return "reviews";
    }
    
    @PostMapping("/reviews")
    public String submitReview(@RequestParam String customerName,
                              @RequestParam String customerEmail,
                              @RequestParam Integer rating,
                              @RequestParam String comment,
                              RedirectAttributes redirectAttributes) {
        try {
            System.out.println("DEBUG: Submitting review - Name: " + customerName + ", Email: " + customerEmail + ", Rating: " + rating);
            
            // Validate input
            if (customerName == null || customerName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please enter your name.");
                return "redirect:/reviews?error=true";
            }
            if (rating == null || rating < 1 || rating > 5) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please select a rating (1-5 stars).");
                return "redirect:/reviews?error=true";
            }
            
            Review review = new Review();
            review.setCustomerName(customerName.trim());
            review.setCustomerEmail(customerEmail != null ? customerEmail.trim() : "");
            review.setRating(rating);
            review.setComment(comment != null ? comment.trim() : "");
            review.setApproved(false);
            
            Review savedReview = reviewRepository.save(review);
            System.out.println("DEBUG: Review saved successfully with ID: " + savedReview.getId());
            
            redirectAttributes.addFlashAttribute("successMessage", "Thank you! Your review has been submitted successfully and is pending approval.");
            return "redirect:/thankyou-review";

        } catch (Exception e) {
            System.out.println("DEBUG: Error saving review: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error submitting review: " + e.getMessage());
            return "redirect:/reviews?error=true";
        }
    }

    @GetMapping("/order")
    public String order(Model model) {
        model.addAttribute("order", new Order());
        
        List<MenuItem> items;
        try {
            items = menuItemRepository.findByIsActiveTrueOrderByNameAsc();
        } catch (Exception e) {
            System.err.println("Error fetching menu items: " + e.getMessage());
            items = Collections.emptyList();
        }
        model.addAttribute("menuItems", items != null ? items : Collections.emptyList());
        
        return "order";
    }
    
    @PostMapping("/order")
    public String processOrder(@ModelAttribute Order order, RedirectAttributes redirectAttributes) {
        try {
            // Validate order
            if (order.getCustomerName() == null || order.getCustomerName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please enter your name.");
                return "redirect:/order?error=true";
            }
            if (order.getPhone() == null || order.getPhone().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please enter your phone number.");
                return "redirect:/order?error=true";
            }
            
            // Set default values
            order.setStatus(Order.OrderStatus.PENDING);
            if (order.getTotalAmount() == null) {
                order.setTotalAmount(BigDecimal.ZERO);
            }
            if (order.getItems() == null || order.getItems().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please add items to your order.");
                return "redirect:/order?error=true";
            }
            
            orderRepository.save(order);
            return "redirect:/thankyou";
            
        } catch (Exception e) {
            System.out.println("DEBUG: Error processing order: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error processing order: " + e.getMessage());
            return "redirect:/order?error=true";
        }
    }
    
    @GetMapping("/thankyou")
    public String thankyou() {
        return "thankyou";
    }
    
    @GetMapping("/thankyou-review")
    public String thankyouReview() {
        return "thankyou-review";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
    
    @GetMapping("/locations")
    public String locations() {
        return "locations";
    }
    
    @GetMapping("/events")
    public String events() {
        return "events";
    }
    
    @GetMapping("/blog")
    public String blog() {
        return "blog";
    }
    
    @GetMapping("/franchise")
    public String franchise() {
        return "franchise";
    }
}

