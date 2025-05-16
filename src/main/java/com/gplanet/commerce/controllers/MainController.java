package com.gplanet.commerce.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class MainController {

  private final ProductoService productoService;

  /**
   * Handles requests to the home page, redirecting based on user role
   * and loading initial product list.
   * 
   * @param authentication Current user's authentication
   * @param model          Spring MVC model
   * @param compraExitosa  Optional parameter indicating purchase success when
   *                       redirected
   * @return View name for home page or redirect URL
   */
  @GetMapping("/")
  public String home(Authentication authentication, Model model,
      @RequestParam(required = false) Boolean compraExitosa) {

    if (log.isDebugEnabled()) {
      log.debug("Accessing home page - User authenticated: {}", 
          authentication != null && authentication.isAuthenticated());
    }

    // Check if user is authenticated
    if (authentication != null && authentication.isAuthenticated()) {
      if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
        log.info("Admin user {} accessing home page, redirecting to admin products", authentication.getName());
        return "redirect:/productos/admin/listar";
      } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER"))) {
        if (log.isDebugEnabled()) {
          log.debug("Regular user {} accessing home page", authentication.getName());
        }
        model.addAttribute("activePage", "productos");
        if (compraExitosa != null) {
          log.info("Purchase status for user {}: {}", authentication.getName(), compraExitosa);
          model.addAttribute("compraExitosa", compraExitosa);
        }
      }
    }

    if (log.isDebugEnabled()) {
      log.debug("Loading initial product list");
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
