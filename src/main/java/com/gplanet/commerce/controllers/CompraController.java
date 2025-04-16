package com.gplanet.commerce.controllers;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gplanet.commerce.dtos.compra.CompraDTO;
import com.gplanet.commerce.dtos.compra.CompraResponseDTO;
import com.gplanet.commerce.dtos.pagination.PaginatedResponse;
import com.gplanet.commerce.services.CompraService;
import com.gplanet.commerce.utilities.ToastUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller class that handles purchase-related operations.
 * Provides endpoints for creating new purchases and viewing purchase history.
 * 
 * @author Gustavo
 * @version 1.0
 */
@Controller
@RequestMapping("/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;

    /**
     * Processes a new purchase request from a user.
     * 
     * @param compraDTO Purchase data transfer object containing purchase details
     * @param bindingResult Validation results for the purchase data
     * @param authentication Current user's authentication
     * @param redirectAttributes Redirect attributes for toast messages
     * @return Redirect URL with purchase status
     */
    @PostMapping("/nueva")
    public String processPurchase(
            @Valid @ModelAttribute CompraDTO compraDTO,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            // Collect error messages
            String errors = bindingResult.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining("\n"));

            ToastUtil.errorRedirect(redirectAttributes, errors);
            return "redirect:/?compraExitosa=false";
        }

        try {
            compraService.realizarCompra(authentication.getName(), compraDTO);
            ToastUtil.successRedirect(redirectAttributes, "Purchase completed successfully");

            return "redirect:/?compraExitosa=true";
        } catch (Exception e) {
            ToastUtil.errorRedirect(redirectAttributes, "Purchase failed: " + e.getMessage());
            return "redirect:/?compraExitosa=false";
        }
    }

    /**
     * Lists all purchases for the current user with pagination.
     * 
     * @param authentication Current user's authentication
     * @param model Spring MVC model
     * @return View name for purchase list
     */
    @GetMapping("/listar")
    public String listarCompras(Authentication authentication, Model model) {
        Page<CompraResponseDTO> comprasPage = compraService.listarCompras(
            authentication.getName(), 0, 10, "fecha", "DESC");
        PaginatedResponse<CompraResponseDTO> paginatedResponse = PaginatedResponse.fromPage(comprasPage);

        model.addAttribute("activePage", "compras");
        model.addAttribute("compras", comprasPage.getContent());
        model.addAttribute("pagination", paginatedResponse);
        return "compras/lista";
    }

    /**
     * Filters and paginates purchase list based on provided parameters.
     * 
     * @param authentication Current user's authentication
     * @param page Page number (zero-based)
     * @param size Items per page
     * @param sort Sort field
     * @param direction Sort direction
     * @param model Spring MVC model
     * @return Fragment name containing filtered results
     */
    @PostMapping("/filtrar")
    public String filterProducts(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fecha") String sort,
            @RequestParam(defaultValue = "DESC") String direction,
            Model model) {
        
        Page<CompraResponseDTO> comprasPage = compraService.listarCompras(
            authentication.getName(), page, size, sort, direction);
        PaginatedResponse<CompraResponseDTO> paginatedResponse = PaginatedResponse.fromPage(comprasPage);

        model.addAttribute("compras", comprasPage.getContent());
        model.addAttribute("pagination", paginatedResponse);
        
        return "compras/page :: compras-page";
    }
}
