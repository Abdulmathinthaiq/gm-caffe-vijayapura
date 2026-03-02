# GM Caffe Website

A complete, mobile-first cafe website with 12 essential pages and a secure Admin Panel built using pure Java Spring Boot, MySQL, Thymeleaf, and Bootstrap 5.

## Features

### Public Pages (12 Pages)
1. **Home** - Hero section, featured items, testimonials
2. **Menu** - Full menu with category filtering
3. **About Us** - Company story, team members
4. **Gallery** - Photo gallery
5. **Reviews** - Customer testimonials with ratings
6. **Order Online** - Quick ordering with WhatsApp integration
7. **Contact** - Contact form and location
8. **Locations** - Multiple branch locations
9. **Events/Catering** - Event booking and catering packages
10. **Blog/News** - News and updates
11. **Franchise** - Franchise inquiry form
12. **Thank You** - Order confirmation page

### Admin Panel
- **Dashboard** - Overview with statistics
- **Menu Management** - CRUD operations for menu items
- **Order Management** - View and update order status
- **Review Management** - Approve/delete customer reviews
- **User Management** - Manage admin users

## Tech Stack
- **Backend**: Spring Boot 3.2 + Spring Security
- **Database**: MySQL 8.0
- **Frontend**: Thymeleaf + Bootstrap 5 + FontAwesome
- **Security**: BCrypt password encryption, Role-based access

## Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher

## Database Setup

1. Create a MySQL database:
```
sql
CREATE DATABASE gm_caffe;
```

2. Update `src/main/resources/application.properties` with your MySQL credentials:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/gm_caffe?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=yourpassword
```

## Running the Application

```
bash
# Navigate to project directory
cd gm-caffe-site

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start at: **http://localhost:8080**

## Default Admin Credentials

- **Username**: admin
- **Password**: admin123

## Admin Panel Access

- **URL**: http://localhost:8080/admin/login
- **Login**: admin / admin123

## Project Structure

```
gm-caffe-site/
├── pom.xml
├── src/
│   └── main/
│       ├── java/com/gmcaffe/
│       │   ├── GmCaffeApplication.java
│       │   ├── config/
│       │   │   ├── SecurityConfig.java
│       │   │   └── DataInitializer.java
│       │   ├── controllers/
│       │   │   ├── PublicController.java
│       │   │   └── AdminController.java
│       │   ├── models/
│       │   │   ├── User.java
│       │   │   ├── MenuItem.java
│       │   │   ├── Order.java
│       │   │   └── Review.java
│       │   └── repositories/
│       │       ├── UserRepository.java
│       │       ├── MenuItemRepository.java
│       │       ├── OrderRepository.java
│       │       └── ReviewRepository.java
│       └── resources/
│           ├── application.properties
│           ├── static/
│           │   ├── css/style.css
│           │   └── js/main.js
│           └── templates/
│               ├── index.html
│               ├── menu.html
│               ├── about.html
│               ├── gallery.html
│               ├── reviews.html
│               ├── order.html
│               ├── contact.html
│               ├── locations.html
│               ├── events.html
│               ├── blog.html
│               ├── franchise.html
│               ├── thankyou.html
│               └── admin/
│                   ├── login.html
│                   ├── dashboard.html
│                   ├── menu.html
│                   ├── orders.html
│                   ├── reviews.html
│                   └── users.html
```

## Customization

### Changing Database Credentials
Edit `src/main/resources/application.properties`:
```
properties
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Adding Menu Items
Log in to the admin panel and navigate to Menu Items to add new items.

### Modifying Static Files
- Styles: `src/main/resources/static/css/style.css`
- Scripts: `src/main/resources/static/js/main.js`

## License

This project is for Business purpose only

