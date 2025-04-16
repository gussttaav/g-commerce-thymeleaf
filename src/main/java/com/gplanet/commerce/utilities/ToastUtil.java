package com.gplanet.commerce.utilities;

import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Utility class for managing toast notifications in the application.
 * Provides methods for adding success and error messages to both
 * regular models and redirect attributes.
 * 
 * @author Gustavo
 * @version 1.0
 */
public class ToastUtil {
    /**
     * Adds a success toast message to the model.
     * 
     * @param model The Spring MVC model
     * @param message The success message to display
     */
    public static void success(Model model, String message) {
        model.addAttribute("toastType", "success");
        model.addAttribute("toastMessage", message);
    }

    /**
     * Adds an error toast message to the model.
     * 
     * @param model The Spring MVC model
     * @param message The error message to display
     */
    public static void error(Model model, String message) {
        model.addAttribute("toastType", "danger");
        model.addAttribute("toastMessage", message);
    }

    /**
     * Adds a success toast message to redirect attributes.
     * 
     * @param redirectAttributes The Spring MVC redirect attributes
     * @param message The success message to display after redirect
     */
    public static void successRedirect(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("toastType", "success");
        redirectAttributes.addFlashAttribute("toastMessage", message);
    }

    /**
     * Adds an error toast message to redirect attributes.
     * 
     * @param redirectAttributes The Spring MVC redirect attributes
     * @param message The error message to display after redirect
     */
    public static void errorRedirect(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("toastType", "danger");
        redirectAttributes.addFlashAttribute("toastMessage", message);
    }
}
