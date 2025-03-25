package com.gplanet.commerce.utilities;

import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class ToastUtil {
    public static void success(Model model, String message) {
        model.addAttribute("toastType", "success");
        model.addAttribute("toastMessage", message);
    }

    public static void error(Model model, String message) {
        model.addAttribute("toastType", "danger");
        model.addAttribute("toastMessage", message);
    }

    public static void successRedirect(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("toastType", "success");
        redirectAttributes.addFlashAttribute("toastMessage", message);
    }

    public static void errorRedirect(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("toastType", "danger");
        redirectAttributes.addFlashAttribute("toastMessage", message);
    }
}
