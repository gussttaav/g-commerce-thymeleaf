/**
 * Utility class to handle theme management
 * Manages:
 * - Theme initialization from user preferences
 * - Theme toggling (light/dark)
 * - Theme persistence in localStorage
 * - Theme button UI updates
 * - System theme detection
 */
class ThemeUtils {
    /**
     * Initializes theme toggle button state
     * - Gets current theme from document
     * - Updates button icon accordingly
     * - Called on page load
     * - Works in conjunction with initial-theme.js
     */
    static initialize() {
        const currentTheme = document.documentElement.getAttribute('data-bs-theme');
        this.updateThemeButton(currentTheme);
    }

    /**
     * Toggles between light and dark themes
     * - Switches between themes
     * - Updates localStorage
     * - Updates button appearance
     * - Called when user clicks theme toggle button
     */
    static toggle() {
        const currentTheme = document.documentElement.getAttribute('data-bs-theme');
        const newTheme = currentTheme === 'light' ? 'dark' : 'light';
        this.applyTheme(newTheme);
    }

    /**
     * Applies the specified theme
     * - Sets theme attribute on document
     * - Saves theme preference
     * - Updates UI to match theme
     * Used by both toggle and initialization
     * @param {string} theme - Theme to apply ('light' | 'dark')
     * @private
     */
    static applyTheme(theme) {
        document.documentElement.setAttribute('data-bs-theme', theme);
        localStorage.setItem('theme', theme);
        this.updateThemeButton(theme);
    }

    /**
     * Updates the theme toggle button appearance
     * - Changes button icon based on theme
     * - Uses Bootstrap icons
     * - Switches between sun and moon icons
     * Called whenever theme changes
     * @param {string} theme - Current theme
     * @private
     */
    static updateThemeButton(theme) {
        const button = document.getElementById('themeToggle');
        const icon = button.querySelector('i');
        if (theme === 'dark') {
            icon.classList.replace('bi-moon-fill', 'bi-sun-fill');
        } else {
            icon.classList.replace('bi-sun-fill', 'bi-moon-fill');
        }
    }
}

// Initialize the theme when the page loads
document.addEventListener('DOMContentLoaded', () => {
    ThemeUtils.initialize();
});

// Add event listener to the theme toggle button
document.getElementById('themeToggle').addEventListener('click', () => {
    ThemeUtils.toggle();
});