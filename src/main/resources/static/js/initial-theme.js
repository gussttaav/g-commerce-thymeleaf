/**
 * Initial theme detection and application
 * Executed before page content loads to prevent flash of wrong theme
 * 
 * Process:
 * 1. Checks localStorage for saved theme preference
 * 2. If no saved preference, checks system color scheme
 * 3. Applies theme to document
 * 4. Saves preference for future visits
 * 
 * This script must be:
 * - Loaded synchronously
 * - Placed in head before any content
 * - Non-module script for immediate execution
 */

const savedTheme = localStorage.getItem('theme');
if (savedTheme) {
    document.documentElement.setAttribute('data-bs-theme', savedTheme);
} else {
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    const systemTheme = prefersDark ? 'dark' : 'light';
    document.documentElement.setAttribute('data-bs-theme', systemTheme);
    localStorage.setItem('theme', systemTheme);
} 