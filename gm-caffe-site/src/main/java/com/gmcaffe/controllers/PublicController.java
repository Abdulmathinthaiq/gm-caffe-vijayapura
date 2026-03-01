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
        List<MenuItem> featuredItems = menuItemRepository.findByIsFeaturedTrueAndIsActiveTrue();
        if (featuredItems.isEmpty()) {
            featuredItems = menuItemRepository.findByIsActiveTrueOrderByNameAsc().stream().limit(6).toList();
        }
        model.addAttribute("featuredItems", featuredItems);
        
        List<Review> recentReviews = reviewRepository.findByIsApprovedTrueOrderByCreatedAtDesc().stream().limit(5).toList();
        model.addAttribute("recentReviews", recentReviews);
        
        Double avgRating = reviewRepository.getAverageRating();
        model.addAttribute("avgRating", avgRating != null ? avgRating : 0);
        
        // Get active offers for marquee
        List<Offer> activeOffers = offerRepository.findByActiveTrueOrderByDisplayOrderAsc();
        model.addAttribute("offers", activeOffers);
        
        return "index";
    }
    
    @GetMapping("/menu")
    public String menu(Model model, @RequestParam(required = false) String category) {
        List<MenuItem> items;
        if (category != null && !category.isEmpty()) {
            items = menuItemRepository.findByCategoryAndIsActiveTrue(category);
        } else {
            items = menuItemRepository.findByIsActiveTrueOrderByNameAsc();
        }
        model.addAttribute("allItems", items);
        
        List<String> categories = menuItemRepository.findAllCategories();
        model.addAttribute("categories", categories);
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
        List<Review> approvedReviews = reviewRepository.findByIsApprovedTrueOrderByCreatedAtDesc();
        model.addAttribute("reviews", approvedReviews);
        
        Double avgRating = reviewRepository.getAverageRating();
        model.addAttribute("avgRating", avgRating != null ? avgRating : 0);
        
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
            Review review = new Review();
            review.setCustomerName(customerName);
            review.setCustomerEmail(customerEmail);
            review.setRating(rating);
            review.setComment(comment);
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
        List<MenuItem> items = menuItemRepository.findByIsActiveTrueOrderByNameAsc();
        model.addAttribute("menuItems", items);
        return "order";
    }
    
    @PostMapping("/order")
    public String processOrder(@ModelAttribute Order order) {
        order.setStatus(Order.OrderStatus.PENDING);
        orderRepository.save(order);
        return "redirect:/thankyou";
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
