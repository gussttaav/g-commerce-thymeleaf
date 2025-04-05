/**
 * Utility class for UI operations
 * Centralizes:
 * - Loading spinner management
 * - Toast notifications
 * - Error/Success messages
 * - Common UI patterns
 * Used across all components for consistent UI feedback
 */
class UiUtils {
    /**
     * Shows a toast notification
     * - Creates toast element dynamically
     * - Adds it to container with animation
     * - Auto-dismisses after timeout
     * - Supports success/error styles
     * - Uses Bootstrap toast component
     * @param {string} message - Message to display
     * @param {string} type - Toast type ('success' | 'error')
     * @private
     */
    static showToast(message, type = 'success') {
        const toastContainer = document.querySelector('.toast-container');
        const toastHtml = `
            <div class="toast align-items-center text-white bg-${type} border-0" role="alert">
                <div class="d-flex">
                    <div class="toast-body">
                        ${message}
                    </div>
                    <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
                </div>
            </div>
        `;
        toastContainer.insertAdjacentHTML('beforeend', toastHtml);
        
        const toastElement = toastContainer.lastElementChild;
        const toast = new bootstrap.Toast(toastElement, { delay: 3000 });
        toast.show();
        
        toastElement.addEventListener('hidden.bs.toast', () => {
            toastElement.remove();
        });
    }

    /**
     * Shows success toast notification
     * - Green styling
     * - Used for positive feedback
     * - Auto-dismisses
     * - Called after successful operations
     * @param {string} message - Success message
     */
    static showSuccess(message) {
        this.showToast(message, 'success');
    }

    /**
     * Shows error toast notification
     * - Red styling
     * - Used for error feedback
     * - Auto-dismisses
     * - Called after failed operations
     * @param {string} message - Error message
     */
    static showError(message) {
        this.showToast(message, 'danger');
    }

    /**
     * Shows loading spinner
     * - Displays centered overlay spinner
     * - Prevents user interaction
     * - Used during async operations
     * - Uses Bootstrap spinner component
     */
    static showSpinner() {
        document.getElementById('loadingSpinner')?.classList.remove('d-none');
    }

    /**
     * Hides loading spinner
     * - Removes spinner overlay
     * - Re-enables user interaction
     * - Called after operation completes
     * - Always called in finally blocks
     */
    static hideSpinner() {
        document.getElementById('loadingSpinner')?.classList.add('d-none');
    }


    /**
     * Shows a Bootstrap modal after it has been loaded via HTMX
     * @param {string} modalId - The ID of the modal element (default: 'dynamicModal')
     */
    static showModal(modalId = 'dynamicModal') {
        const modalElement = document.getElementById(modalId);
        if (modalElement) {
            const modal = new bootstrap.Modal(modalElement);
            modal.show();
        } else {
            console.error(`Modal element with ID '${modalId}' not found`);
        }
    }

    /**
     * Closes a Bootstrap modal and cleans up the container
     * @param {string} modalId - The ID of the modal element (default: 'dynamicModal')
     * @param {string} containerId - The ID of the container element (default: 'modalContainer')
     */
    static closeModal(modalId = 'dynamicModal', containerId = 'modalContainer') {
        const modalElement = document.getElementById(modalId);
        if (modalElement) {
            // Try to use Bootstrap's API first
            const modal = bootstrap.Modal.getInstance(modalElement);
            if (modal) {
                modal.hide();
            } else {
                // Fallback if the Bootstrap API isn't available
                modalElement.classList.remove('show');
                modalElement.style.display = 'none';
                document.body.classList.remove('modal-open');
                
                // Remove modal backdrop
                const modalBackdrops = document.getElementsByClassName('modal-backdrop');
                while (modalBackdrops.length > 0) {
                    modalBackdrops[0].parentNode.removeChild(modalBackdrops[0]);
                }
            }
            
            // Clean up the container after hiding
            if (containerId) {
                setTimeout(function() {
                    const container = document.getElementById(containerId);
                    if (container) {
                        container.innerHTML = '';
                    }
                }, 300);
            }
        }
    }

    /**
     * Setup HTMX event listeners for modals
     * Maps modal IDs to their container IDs
     * @param {Object} modalMappings - Object mapping modal IDs to container IDs
     */
    static setupModalListeners(modalMappings = { 'dynamicModal': 'modalContainer' }) {
        document.addEventListener('htmx:afterSwap', function(event) {
            // Check if the target is one of our modal containers
            const targetId = event.detail.target.id;
            
            // Find which modal corresponds to this container
            for (const [modalId, containerId] of Object.entries(modalMappings)) {
                if (targetId === containerId) {
                    UiUtils.showModal(modalId);
                    break;
                }
            }
        });
    }
} 

document.addEventListener('DOMContentLoaded', function() {
    // Check for toast message from server
    const toastType = document.body.getAttribute('data-toast-type');
    const toastMessage = document.body.getAttribute('data-toast-message');

    if (toastType && toastMessage) {
        // Use the UiUtils class you already have
        UiUtils.showToast(toastMessage, toastType);
    }
});