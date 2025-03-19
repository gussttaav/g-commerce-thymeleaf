package com.gplanet.commerce.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.gplanet.commerce.services.ProductoService;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final ProductoService productoService;
    
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("productos", productoService.findAllActiveProductos());
        return "index";
    }
}
