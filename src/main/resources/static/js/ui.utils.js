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