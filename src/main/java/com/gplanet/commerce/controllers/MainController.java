package com.gplanet.commerce.controllers;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gplanet.commerce.dtos.producto.ProductStatus;
import com.gplanet.commerce.dtos.producto.ProductoResponseDTO;
import com.gplanet.commerce.services.ProductoService;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final ProductoService productoService;
    
    @GetMapping("/")
    public String home(Authentication authentication, Model model,
                       @RequestParam(required = false) Boolean compraExitosa) {
        List<ProductoResponseDTO> productos = null;

        // Check if user is authenticated
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                model.addAttribute("activePage", "adminProductos");
                productos = productoService.listarProductos(ProductStatus.ALL);
            }
            else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER"))) {
                model.addAttribute("activePage", "productos");
                if(compraExitosa != null){
                    model.addAttribute("compraExitosa", compraExitosa);
                }
                productos = productoService.listarProductos(ProductStatus.ACTIVE);
            }
        }

        if(productos == null){
            productos = productoService.listarProductos(ProductStatus.ACTIVE);
        }

        model.addAttribute("productos", productos);
        return "index";
    }
}
