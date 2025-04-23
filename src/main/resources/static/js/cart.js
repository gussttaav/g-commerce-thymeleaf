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
        const clearCart = document.querySelector('span[data-cart-empty]');
        if (clearCart && clearCart.dataset.cartEmpty === 'true') {
            this.clear();
            return;
        }

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
        document.addEventListener('click', e => {
            if (e.target.closest('.add-to-cart-btn')) {
                const button = e.target.closest('.add-to-cart-btn');
                const productId = parseInt(button.dataset.productId);
                const card = button.closest('.card');
                const quantityInput = card.querySelector('.quantity-input');
                const quantity = parseInt(quantityInput.value);
                
                const product = {
                    id: productId,
                    nombre: card.querySelector('.card-title').textContent,
                    precio: parseFloat(card.querySelector('.price-tag').textContent.replace('$', '')),
                    descripcion: card.querySelector('.card-text').textContent
                };
                
                this.addProduct(product, quantity);
            }
            
            // Quantity buttons delegation
            if (e.target.closest('.quantity-btn')) {
                const button = e.target.closest('.quantity-btn');
                const input = button.parentElement.querySelector('.quantity-input');
                const currentValue = parseInt(input.value);
                
                if (button.dataset.action === 'increase') {
                    input.value = currentValue + 1;
                } else if (button.dataset.action === 'decrease' && currentValue > 1) {
                    input.value = currentValue - 1;
                }
            }
        });
    }

    /**
     * Initializes cart event listeners
     * @private
     */
    initializeEventListeners() {
        
        document.getElementById('cartButton')?.addEventListener('click', e => {
            e.preventDefault();
            const cartModal = new bootstrap.Modal(document.getElementById('cartModal'));
            cartModal.show();
        });

        document.getElementById('proceedToCheckoutBtn')?.addEventListener('click', async () => {
            try {
                const response = await fetch('/usuarios/authenticated');
                const isAuthenticated = await response.json();

                if (isAuthenticated) {
                    this.showCheckoutModal();
                } else {
                    window.location.href = '/usuarios/login';
                }
            } catch (error) {
                console.error('Authentication check failed:', error);
                window.location.href = '/usuarios/login';
            }
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

        // Prepare a hidden form for checkout submission
        this.prepareCheckoutForm();
    }

    prepareCheckoutForm() {
        // Obtain CSRF token afrom meta tag
        const csrfToken = document.querySelector('meta[name="_csrf"]').content;

        // Create a hidden form if it doesn't exist
        let checkoutForm = document.getElementById('checkoutForm');
        if (!checkoutForm) {
            checkoutForm = document.createElement('form');
            checkoutForm.id = 'checkoutForm';
            checkoutForm.method = 'POST';
            checkoutForm.action = '/compras/nueva';
            document.body.appendChild(checkoutForm);
        }

        // Add event listener to confirm purchase button
        document.getElementById('confirmPurchaseBtn')?.addEventListener('click', () => {
            if (this.selectedProducts.size === 0) {
                UiUtils.showError('Your cart is empty');
                return;
            }

            // Clear existing form inputs
            checkoutForm.innerHTML = '';

            // Add CSRF token
            const csrfInput = document.createElement('input');
            csrfInput.type = 'hidden';
            csrfInput.name = '_csrf';
            csrfInput.value = csrfToken;
            checkoutForm.appendChild(csrfInput);

            // Add cart items as hidden inputs following the DTO structure
            this.selectedProducts.forEach((item, productId) => {
                // Producto ID input
                const productIdInput = document.createElement('input');
                productIdInput.type = 'hidden';
                productIdInput.name = 'productos[' + (Array.from(this.selectedProducts.keys()).indexOf(productId)) + '].productoId';
                productIdInput.value = productId;
                checkoutForm.appendChild(productIdInput);

                // Cantidad input
                const cantidadInput = document.createElement('input');
                cantidadInput.type = 'hidden';
                cantidadInput.name = 'productos[' + (Array.from(this.selectedProducts.keys()).indexOf(productId)) + '].cantidad';
                cantidadInput.value = item.quantity;
                checkoutForm.appendChild(cantidadInput);
            });

            // Submit the form
            checkoutForm.submit();
        });
    }

    showCheckoutModal() {
        const detailsContainer = document.getElementById('purchaseDetails');
        const totalElement = document.getElementById('purchaseTotal');

        if(detailsContainer === null || totalElement === null){
            return;
        }
        
        detailsContainer.innerHTML = '';
        this.selectedProducts.forEach(item => {
            const itemDiv = document.createElement('div');
            itemDiv.className = 'mb-2';
            itemDiv.innerHTML = `
                <div class="d-flex justify-content-between">
                    <span>${item.nombre} x ${item.quantity}</span>
                    <span>$${(item.precio * item.quantity).toFixed(2)}</span>
                </div>
            `;
            detailsContainer.appendChild(itemDiv);
        });

        totalElement.textContent = this.calculateTotal().toFixed(2);
        new bootstrap.Modal(document.getElementById('checkoutModal')).show();
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