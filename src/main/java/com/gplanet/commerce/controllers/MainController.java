package com.gplanet.commerce.controllers;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gplanet.commerce.dtos.pagination.PaginatedResponse;
import com.gplanet.commerce.dtos.producto.ProductStatus;
import com.gplanet.commerce.dtos.producto.ProductoResponseDTO;
import com.gplanet.commerce.services.ProductoService;

/**
 * Main controller class that handles the application's home page
 * and initial routing based on user role.
 * 
 * @author Gustavo
 * @version 1.0
 */
@Controller
@RequiredArgsConstructor
public class MainController {

    private final ProductoService productoService;
    
    /**
     * Handles requests to the home page, redirecting based on user role
     * and loading initial product list.
     * 
     * @param authentication Current user's authentication
     * @param model Spring MVC model
     * @param compraExitosa Optional parameter indicating purchase success when redirected
     * @return View name for home page or redirect URL
     */
    @GetMapping("/")
    public String home(Authentication authentication, Model model,
                       @RequestParam(required = false) Boolean compraExitosa) {
        
        // Check if user is authenticated
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                return "redirect:/productos/admin/listar";
            }
            else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER"))) {
                model.addAttribute("activePage", "productos");
                if(compraExitosa != null){
                    model.addAttribute("compraExitosa", compraExitosa);
                }
            }
        }

        // Initial product loading with default values
        Page<ProductoResponseDTO> productosPage = productoService.listarProductos(
            ProductStatus.ACTIVE, "", 0, 10, "nombre", "ASC");
        
        PaginatedResponse<ProductoResponseDTO> paginatedResponse = PaginatedResponse.fromPage(productosPage);
        
        model.addAttribute("productos", productosPage.getContent());
        model.addAttribute("pagination", paginatedResponse);
        
        return "index";
    }
}
