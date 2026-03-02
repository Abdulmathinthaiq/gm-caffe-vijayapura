// GM Caffe - Main JavaScript File

// Cart functionality with localStorage for persistence
let cart = [];

// Initialize cart from localStorage on page load
function initCart() {
    const storedCart = localStorage.getItem('gmCaffeCart');
    if (storedCart) {
        try {
            cart = JSON.parse(storedCart);
        } catch (e) {
            cart = [];
        }
    }
    updateCartDisplay();
}

// Save cart to localStorage
function saveCart() {
    localStorage.setItem('gmCaffeCart', JSON.stringify(cart));
}

// Clear cart
function clearCart() {
    cart = [];
    localStorage.removeItem('gmCaffeCart');
    updateCartDisplay();
}

function addToOrder(name, price) {
    // Check if item already exists
    const existingItem = cart.find(item => item.name === name);
    if (existingItem) {
        existingItem.quantity += 1;
    } else {
        cart.push({ name: name, price: parseFloat(price), quantity: 1 });
    }
    saveCart();
    updateCartDisplay();
    showToast(`Added ${name} to cart!`);
}

function updateCartDisplay() {
    // Update cart badge
    const cartBadge = document.getElementById('cart-badge');
    if (cartBadge) {
        const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
        cartBadge.textContent = totalItems;
    }
    
    // Update order page cart if on order page
    const cartContainer = document.getElementById('cartContainer');
    if (cartContainer) {
        updateOrderPageCart();
    }
}

function updateOrderPageCart() {
    const cartItemsDiv = document.getElementById('cartItems');
    const emptyMsg = document.getElementById('emptyCartMsg');
    const cartTotalSpan = document.getElementById('cartTotal');
    const orderItemsInput = document.getElementById('orderItems');
    const totalAmountInput = document.getElementById('totalAmountInput');
    
    if (!cartItemsDiv) return;
    
    if (cart.length === 0) {
        if (emptyMsg) emptyMsg.style.display = 'block';
        cartItemsDiv.innerHTML = '';
        if (cartTotalSpan) cartTotalSpan.textContent = '0.00';
        if (orderItemsInput) orderItemsInput.value = '';
        if (totalAmountInput) totalAmountInput.value = '0';
        return;
    }
    
    if (emptyMsg) emptyMsg.style.display = 'none';
    
    let total = 0;
    let itemsText = [];
    let html = '<table class="table table-sm mb-0"><tbody>';
    
    cart.forEach((item, index) => {
        const itemTotal = item.price * item.quantity;
        total += itemTotal;
        itemsText.push(`${item.name} x${item.quantity} (₹${itemTotal.toFixed(2)})`);
        
        html += `
            <tr>
                <td>${item.name}</td>
                <td>₹${item.price.toFixed(2)}</td>
                <td>
                    <div class="btn-group btn-group-sm">
                        <button type="button" class="btn btn-outline-secondary" onclick="updateQuantity(${index}, -1)">-</button>
                        <span class="btn btn-light disabled">${item.quantity}</span>
                        <button type="button" class="btn btn-outline-secondary" onclick="updateQuantity(${index}, 1)">+</button>
                    </div>
                </td>
                <td class="text-end">₹${itemTotal.toFixed(2)}</td>
                <td class="text-end">
                    <button type="button" class="btn btn-sm btn-danger" onclick="removeItem(${index})">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            </tr>
        `;
    });
    
    html += '</tbody></table>';
    cartItemsDiv.innerHTML = html;
    if (cartTotalSpan) cartTotalSpan.textContent = total.toFixed(2);
    if (orderItemsInput) orderItemsInput.value = itemsText.join(', ');
    if (totalAmountInput) totalAmountInput.value = total.toFixed(2);
}

function removeItem(index) {
    cart.splice(index, 1);
    saveCart();
    updateCartDisplay();
    showToast('Item removed from cart!');
}

function updateQuantity(index, change) {
    cart[index].quantity += change;
    if (cart[index].quantity <= 0) {
        removeItem(index);
    } else {
        saveCart();
        updateCartDisplay();
    }
}

function showToast(message) {
    const toast = document.createElement('div');
    toast.className = 'toast-notification';
    toast.textContent = message;
    toast.style.cssText = `
        position: fixed;
        top: 80px;
        right: 20px;
        background: #ffc107;
        color: #1a1a1a;
        padding: 15px 25px;
        border-radius: 5px;
        box-shadow: 0 4px 10px rgba(0,0,0,0.2);
        z-index: 1000;
        animation: slideIn 0.3s ease;
    `;
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 2000);
}

// Add item to order in order page
function addItem(item) {
    const textarea = document.getElementById('orderItems');
    if (textarea) {
        textarea.value = textarea.value ? textarea.value + '\n' + item : item;
    }
}

// Smooth scrolling for anchor links
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function(e) {
        const href = this.getAttribute('href');
        // Only handle if it's a valid anchor link pointing to an element on the page
        if (href && href.length > 1 && href.startsWith('#')) {
            const target = document.querySelector(href);
            if (target) {
                e.preventDefault();
                target.scrollIntoView({
                    behavior: 'smooth'
                });
            }
        }
    });
});


// Navbar scroll effect
window.addEventListener('scroll', function() {
    const navbar = document.querySelector('.navbar');
    if (navbar) {
        if (window.scrollY > 50) {
            navbar.classList.add('shadow');
        } else {
            navbar.classList.remove('shadow');
        }
    }
});

// Form validation
const forms = document.querySelectorAll('form');
forms.forEach(form => {
    form.addEventListener('submit', function(e) {
        if (!form.checkValidity()) {
            e.preventDefault();
            e.stopPropagation();
        }
        form.classList.add('was-validated');
    });
});

// Image lazy loading
if ('IntersectionObserver' in window) {
    const imageObserver = new IntersectionObserver((entries, observer) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const img = entry.target;
                img.src = img.dataset.src || img.src;
                imageObserver.unobserve(img);
            }
        });
    });
    
    document.querySelectorAll('img[data-src]').forEach(img => {
        imageObserver.observe(img);
    });
}

// Gallery lightbox (simple implementation)
const galleryImages = document.querySelectorAll('.gallery-item img');
galleryImages.forEach(img => {
    img.addEventListener('click', function() {
        const modal = document.createElement('div');
        modal.className = 'gallery-modal';
        modal.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0,0,0,0.9);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 2000;
            cursor: pointer;
        `;
        
        const modalImg = document.createElement('img');
        modalImg.src = this.src;
        modalImg.style.maxWidth = '90%';
        modalImg.style.maxHeight = '90%';
        
        modal.appendChild(modalImg);
        document.body.appendChild(modal);
        
        modal.addEventListener('click', () => modal.remove());
    });
});

// Star rating hover effect for reviews
const ratingSelects = document.querySelectorAll('select[name="rating"]');
ratingSelects.forEach(select => {
    select.addEventListener('change', function() {
        // Could add visual feedback here
    });
});

// Counter animation for stats
function animateCounter(element, target) {
    let current = 0;
    const increment = target / 50;
    const timer = setInterval(() => {
        current += increment;
        if (current >= target) {
            element.textContent = target;
            clearInterval(timer);
        } else {
            element.textContent = Math.floor(current);
        }
    }, 30);
}

// Initialize counters on page load
document.addEventListener('DOMContentLoaded', function() {
    const counters = document.querySelectorAll('.counter');
    counters.forEach(counter => {
        const target = parseInt(counter.dataset.target);
        if (target) {
            animateCounter(counter, target);
        }
    });
    
    // Add to order button click handlers (for data attributes)
    const addToOrderBtns = document.querySelectorAll('.add-to-order-btn');
    addToOrderBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            const name = this.dataset.name;
            const price = parseFloat(this.dataset.price);
            if (name && !isNaN(price)) {
                addToOrder(name, price);
            }
        });
    });
    
    // Add item button click handlers for order page (for data attributes)
    const addItemBtns = document.querySelectorAll('.add-item-btn');
    addItemBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            const name = this.dataset.name;
            const price = this.dataset.price;
            if (name && price) {
                addToOrder(name, parseFloat(price));
            }
        });
    });

});


// Mobile menu close on link click
const navLinks = document.querySelectorAll('.navbar-nav .nav-link');
const navbarCollapse = document.querySelector('.navbar-collapse');
navLinks.forEach(link => {
    link.addEventListener('click', () => {
        if (navbarCollapse && navbarCollapse.classList.contains('show')) {
            navbarCollapse.classList.remove('show');
        }
    });
});

// Add animation keyframes dynamically
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    @keyframes slideOut {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }
    
    .navbar.shadow {
        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    }
    
    .toast-notification {
        font-weight: 500;
    }
`;
document.head.appendChild(style);

console.log('GM Caffe - Website loaded successfully!');
