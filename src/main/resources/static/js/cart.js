/**
 * Component to handle shopping cart functionality
 * Manages:
 * - Cart state and storage
 * - Product addition/removal
 * - Quantity updates
 * - Total calculation
 * - Cart UI rendering
 * - Checkout process
 */
class CartComponent {
    constructor() {
        this.selectedProducts = new Map();
        document.addEventListener('DOMContentLoaded', () => {
            this.loadCartFromStorage();
            this.initializeEventListeners();
            this.setupProductPageListeners();
        });
    }

    /**
     * Loads cart data from localStorage
     * Called on initialization
     * @private
     */
    loadCartFromStorage() {
        const storedCart = localStorage.getItem('cartItems');
        if (storedCart) {
            try {
                const cartItemsArray = JSON.parse(storedCart);
                // Convert array back to Map with numeric keys
                this.selectedProducts = new Map(
                    cartItemsArray.map(([key, value]) => [parseInt(key), value])
                );
                this.updateCartCount();
            } catch (e) {
                console.error('Error parsing cart data:', e);
                this.selectedProducts = new Map();
            }
        }
    }

    /**
     * Saves cart data to localStorage
     * Called after any cart modification
     * @private
     */
    saveCartToStorage() {
        try {
            const cartData = JSON.stringify(Array.from(this.selectedProducts.entries()));
            localStorage.setItem('cartItems', cartData);
        } catch (e) {
            console.error('Error saving cart to storage:', e);
        }
    }

    /**
     * Adds a product to the cart
     * - Checks if product already exists
     * - Updates quantity if exists
     * - Adds new item if not
     * - Updates cart badge
     * - Shows success message
     * @param {Object} product - Product to add
     * @param {number} quantity - Quantity to add
     */
    addProduct(product, quantity) {
        if (quantity < 1) {
            UiUtils.showError('Quantity must be greater than 0');
            throw new Error('Quantity must be greater than 0');
        }

        if (this.selectedProducts.has(product.id)) {
            const currentItem = this.selectedProducts.get(product.id);
            this.selectedProducts.set(product.id, {
                ...currentItem,
                quantity: currentItem.quantity + quantity
            });
        } else {
            this.selectedProducts.set(product.id, {
                ...product,
                quantity
            });
        }

        this.saveCartToStorage();
        this.updateCartCount();

        UiUtils.showSuccess(`Added ${quantity} ${product.nombre} to cart`);
    }

    /**
     * Updates the quantity of a product in the cart
     * - Validates new quantity
     * - Updates total price
     * - Re-renders cart
     * - Removes product if quantity is 0
     * @param {number} productId - Product ID
     * @param {number} newQuantity - New quantity
     */
    updateQuantity(productId, newQuantity) {
        if (newQuantity < 1) {
            this.removeProduct(productId);
            return;
        }

        const item = this.selectedProducts.get(productId);
        if (item) {
            this.selectedProducts.set(productId, {
                ...item,
                quantity: newQuantity
            });
            this.saveCartToStorage();
            this.updateCartCount();
        }
    }

    /**
     * Removes a product from the cart
     * - Removes entire product entry
     * - Updates cart badge
     * - Re-renders cart if modal open
     * - Shows feedback message
     * @param {number} productId - ID of product to remove
     */
    removeProduct(productId) {
        const product = this.selectedProducts.get(productId);
        if (product) {
            this.selectedProducts.delete(productId);
            this.saveCartToStorage();
            this.updateCartCount();
        }
    }

    /**
     * Calculates the total price of items in cart
     * - Sums all product prices
     * - Multiplies by quantities
     * - Returns formatted total
     * - Used in cart display and checkout
     * @returns {number} Total cart value
     */
    calculateTotal() {
        let total = 0;
        this.selectedProducts.forEach(item => {
            total += item.precio * item.quantity;
        });
        return total;
    }

    /**
     * Updates the cart count display
     */
    updateCartCount() {
        const count = Array.from(this.selectedProducts.values())
            .reduce((sum, item) => sum + item.quantity, 0);
        document.getElementById('cartCount').textContent = count;
        this.renderCartModal();
    }

    /**
     * Renders the cart modal content
     * - Creates product list HTML
     * - Shows empty cart message if needed
     * - Updates total price
     * - Handles quantity controls
     * - Uses Bootstrap modal
     */
    renderCartModal() {
        const tbody = document.getElementById('cartItems');
        const totalElement = document.getElementById('cartTotal');
        if (!tbody || !totalElement) return;

        tbody.innerHTML = '';

        if (this.selectedProducts.size === 0) {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td colspan="5" class="text-center py-3">
                    Your cart is empty
                </td>
            `;
            tbody.appendChild(tr);
            totalElement.textContent = '0.00';
            document.getElementById('proceedToCheckoutBtn').disabled = true;
            return;
        }

        document.getElementById('proceedToCheckoutBtn').disabled = false;

        this.selectedProducts.forEach((item, productId) => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${item.nombre}</td>
                <td>$${item.precio}</td>
                <td>
                    <div class="input-group input-group-sm" style="width: 120px">
                        <button class="btn btn-outline-secondary quantity-decrease" 
                                data-product-id="${productId}">-</button>
                        <input type="number" class="form-control text-center quantity-input" 
                               value="${item.quantity}" min="1" 
                               data-product-id="${productId}">
                        <button class="btn btn-outline-secondary quantity-increase"
                                data-product-id="${productId}">+</button>
                    </div>
                </td>
                <td>$${(item.precio * item.quantity).toFixed(2)}</td>
                <td>
                    <button class="btn btn-sm btn-danger remove-product" 
                            data-product-id="${productId}">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            `;
            tbody.appendChild(tr);
        });

        totalElement.textContent = this.calculateTotal().toFixed(2);
    }

    /**
     * Clears the cart
     * - Empties products array
     * - Updates cart badge
     * - Called after successful purchase
     * - Called on logout
     */
    clear() {
        this.selectedProducts.clear();
        localStorage.removeItem('cartItems');
        this.updateCartCount();
    }


    setupProductPageListeners() {
        // Add to cart buttons on product page
        document.querySelectorAll('.add-to-cart-btn').forEach(button => {
            button.addEventListener('click', e => {
                const productId = parseInt(e.currentTarget.dataset.productId);
                const card = e.currentTarget.closest('.card');
                const quantityInput = card.querySelector('.quantity-input');
                const quantity = parseInt(quantityInput.value);
                
                // Gather product data from the card
                const product = {
                    id: productId,
                    nombre: card.querySelector('.card-title').textContent,
                    precio: parseFloat(card.querySelector('.price-tag').textContent.replace('$', '')),
                    descripcion: card.querySelector('.card-text').textContent
                };
                
                this.addProduct(product, quantity);
            });
        });
        
        // Product quantity buttons
        document.querySelectorAll('.quantity-btn').forEach(button => {
            button.addEventListener('click', e => {
                const input = e.currentTarget.parentElement.querySelector('.quantity-input');
                const currentValue = parseInt(input.value);
                
                if (e.currentTarget.dataset.action === 'increase') {
                    input.value = currentValue + 1;
                } else if (e.currentTarget.dataset.action === 'decrease' && currentValue > 1) {
                    input.value = currentValue - 1;
                }
            });
        });
        
        // Cart button
        document.getElementById('cartButton')?.addEventListener('click', e => {
            e.preventDefault();
            const cartModal = new bootstrap.Modal(document.getElementById('cartModal'));
            cartModal.show();
        });
    }

    /**
     * Initializes cart event listeners
     * @private
     */
    initializeEventListeners() {
        // Cart modal buttons
        document.getElementById('proceedToCheckoutBtn')?.addEventListener('click', () => {
            bootstrap.Modal.getInstance(document.getElementById('cartModal')).hide();
            window.location.href = '/usuarios/login';
        });

        // Event delegation for cart item controls
        document.getElementById('cartItems')?.addEventListener('click', (e) => {
            let target = e.target;
            let productId;
            
            // Check if clicked on an icon inside a button
            if (target.tagName === 'I' && target.parentElement.hasAttribute('data-product-id')) {
                target = target.parentElement;
            }
            
            // Now get the product ID
            if (target.hasAttribute('data-product-id')) {
                productId = parseInt(target.getAttribute('data-product-id'));
            } else {
                return;
            }

            if (target.classList.contains('quantity-decrease')) {
                const item = this.selectedProducts.get(productId);
                if (item) this.updateQuantity(productId, item.quantity - 1);
            }
            else if (target.classList.contains('quantity-increase')) {
                const item = this.selectedProducts.get(productId);
                if (item) this.updateQuantity(productId, item.quantity + 1);
            }
            else if (target.classList.contains('remove-product')) {
                this.removeProduct(productId);
            }
        });

        // Handle quantity input changes in cart
        document.getElementById('cartItems')?.addEventListener('change', (e) => {
            if (e.target.matches('.quantity-input')) {
                const productId = parseInt(e.target.dataset.productId);
                const newQuantity = parseInt(e.target.value);
                if (!isNaN(newQuantity) && newQuantity > 0) {
                    this.updateQuantity(productId, newQuantity);
                } else {
                    // Reset to previous valid value
                    const item = this.selectedProducts.get(productId);
                    if (item) e.target.value = item.quantity;
                }
            }
        });
    }

    /**
     * Gets all products in cart
     * - Returns copy of products array
     * - Used for checkout
     * - Used for cart rendering
     * @returns {Array} List of products
     */
    getProducts() {
        return Array.from(this.selectedProducts.values());
    }
}

// Initialize cart component
const cartComponent = new CartComponent();