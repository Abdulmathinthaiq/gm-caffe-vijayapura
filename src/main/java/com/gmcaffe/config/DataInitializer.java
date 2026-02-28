package com.gmcaffe.config;

import com.gmcaffe.repositories.MenuItemRepository;
import com.gmcaffe.repositories.OfferRepository;
import com.gmcaffe.repositories.OrderRepository;
import com.gmcaffe.repositories.UserRepository;
import com.gmcaffe.models.MenuItem;
import com.gmcaffe.models.Offer;
import com.gmcaffe.models.Order;
import com.gmcaffe.models.User;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataInitializer {
    
    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, 
                                   MenuItemRepository menuItemRepository,
                                   OfferRepository offerRepository,
                                   OrderRepository orderRepository,
                                   PasswordEncoder passwordEncoder) {

        return args -> {
            // Create admin user if not exists
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@gmcaffe.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(User.Role.ADMIN);
                admin.setFullName("Admin User");
                userRepository.save(admin);
                System.out.println("Admin user created: admin / admin123");
            }
            
            // Create sample menu items if not exists
            if (menuItemRepository.count() == 0) {
                List<MenuItem> menuItems = List.of(
                    createMenuItem("Classic Espresso", "Coffee", new BigDecimal("80"), 
                        "Rich and bold single shot espresso", true, true),
                    createMenuItem("Cappuccino", "Coffee", new BigDecimal("120"),
                        "Smooth espresso with steamed milk foam", true, true),
                    createMenuItem("Latte", "Coffee", new BigDecimal("140"),
                        "Creamy espresso with steamed milk", true, true),
                    createMenuItem("Americano", "Coffee", new BigDecimal("100"),
                        "Espresso with hot water", true, false),
                    createMenuItem("Mocha", "Coffee", new BigDecimal("160"),
                        "Espresso with chocolate and steamed milk", true, true),
                    createMenuItem("Cold Coffee", "Cold Drinks", new BigDecimal("150"),
                        "Chilled coffee with ice cream", true, true),
                    createMenuItem("Iced Latte", "Cold Drinks", new BigDecimal("160"),
                        "Chilled latte with ice", true, false),
                    createMenuItem("Masala Chai", "Tea", new BigDecimal("60"),
                        "Traditional Indian spiced tea", true, true),
                    createMenuItem("Green Tea", "Tea", new BigDecimal("80"),
                        "Healthy green tea", true, false),
                    createMenuItem("Croissant", "Bakery", new BigDecimal("120"),
                        "Buttery French pastry", true, true),
                    createMenuItem("Blueberry Muffin", "Bakery", new BigDecimal("100"),
                        "Fresh baked muffin", true, false),
                    createMenuItem("Chocolate Cake", "Bakery", new BigDecimal("150"),
                        "Rich chocolate cake slice", true, true)
                );
                
                menuItemRepository.saveAll(menuItems);
                System.out.println("Sample menu items created: " + menuItems.size() + " items");
            }
            
            // Create sample offers if not exists - check specifically for offers
            List<Offer> existingOffers = offerRepository.findAll();
            if (existingOffers.isEmpty()) {
                List<Offer> offers = List.of(
                    createOffer("50% OFF on All Coffees!", "Get 50% discount on all coffee drinks this weekend", 
                        "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=400", "/order", true, 1),
                    createOffer("Buy 2 Get 1 Free", "Buy any 2 croissants and get 1 free",
                        "https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=400", "/menu", true, 2),
                    createOffer("Free Delivery", "Free delivery on orders above ₹200",
                        "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=400", "/order", true, 3),
                    createOffer("20% Student Discount", "Show your student ID and get 20% off",
                        "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=400", "/order", true, 4)
                );
                
                offerRepository.saveAll(offers);
                System.out.println("Sample offers created: " + offers.size() + " offers");
            } else {
                System.out.println("Offers already exist: " + existingOffers.size() + " offers found in database");
                // Ensure at least some offers are active for display
                boolean hasActiveOffers = existingOffers.stream().anyMatch(Offer::isActive);
                if (!hasActiveOffers) {
                    // Activate all offers if none are active
                    for (Offer offer : existingOffers) {
                        offer.setActive(true);
                    }
                    offerRepository.saveAll(existingOffers);
                    System.out.println("Activated all existing offers");
                }
            }
            
            // Create sample orders if not exists
            if (orderRepository.count() == 0) {
                List<Order> orders = List.of(
                    createOrder("Rahul Sharma", "9876543210", "2x Cappuccino, 1x Croissant", new BigDecimal("360"), "123 Main St, Bangalore", Order.OrderStatus.PENDING),
                    createOrder("Priya Patel", "9988776655", "1x Latte, 1x Cold Coffee, 1x Chocolate Cake", new BigDecimal("460"), "456 Oak Ave, Bangalore", Order.OrderStatus.PREPARING),
                    createOrder("Amit Kumar", "9123456789", "3x Masala Chai, 2x Blueberry Muffin", new BigDecimal("260"), "789 Pine Rd, Bangalore", Order.OrderStatus.READY),
                    createOrder("Sneha Reddy", "9876123450", "1x Mocha, 1x Cappuccino", new BigDecimal("280"), "321 Elm St, Bangalore", Order.OrderStatus.DELIVERED),
                    createOrder("Vikram Singh", "9988771122", "2x Classic Espresso, 1x Green Tea", new BigDecimal("220"), "654 Maple Dr, Bangalore", Order.OrderStatus.PENDING),
                    createOrder("Anjali Nair", "9900112233", "1x Iced Latte, 1x Cold Coffee", new BigDecimal("310"), "987 Cedar Ln, Bangalore", Order.OrderStatus.PREPARING)
                );
                
                orderRepository.saveAll(orders);
                System.out.println("Sample orders created: " + orders.size() + " orders");
            }
        };
    }
    
    private MenuItem createMenuItem(String name, String category, BigDecimal price, 
                                    String description, boolean isActive, boolean isFeatured) {
        MenuItem item = new MenuItem();
        item.setName(name);
        item.setCategory(category);
        item.setPrice(price);
        item.setDescription(description);
        item.setActive(isActive);
        item.setFeatured(isFeatured);
        item.setImageUrl("https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=400");
        return item;
    }
    
    private Order createOrder(String customerName, String phone, String items, BigDecimal totalAmount, String address, Order.OrderStatus status) {
        Order order = new Order();
        order.setCustomerName(customerName);
        order.setPhone(phone);
        order.setItems(items);
        order.setTotalAmount(totalAmount);
        order.setAddress(address);
        order.setStatus(status);
        order.setOrderedAt(LocalDateTime.now().minusHours((long)(Math.random() * 24))); // Random time within last 24 hours
        return order;
    }
    
    private Offer createOffer(String title, String description, String imageUrl, String linkUrl, boolean active, int displayOrder) {
        Offer offer = new Offer();
        offer.setTitle(title);
        offer.setDescription(description);
        offer.setImageUrl(imageUrl);
        offer.setLinkUrl(linkUrl);
        offer.setActive(active);
        offer.setDisplayOrder(displayOrder);
        return offer;
    }
}
