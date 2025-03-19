package com.gplanet.commerce.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gplanet.commerce.entities.Producto;
import com.gplanet.commerce.services.ProductoService;

import java.util.Optional;

@Controller
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;
    
    @GetMapping
    public String listarProductos(Model model) {
        model.addAttribute("productos", productoService.findAllProductos());
        return "productos/lista";
    }
    
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("producto", new Producto());
        return "productos/formulario";
    }
    
    @PostMapping("/guardar")
    public String guardarProducto(@Valid Producto producto, BindingResult result, RedirectAttributes attributes) {
        if (result.hasErrors()) {
            return "productos/formulario";
        }
        
        productoService.saveProducto(producto);
        attributes.addFlashAttribute("mensaje", "Producto guardado correctamente");
        return "redirect:/productos";
    }
    
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Optional<Producto> producto = productoService.findProductoById(id);
        if (producto.isPresent()) {
            model.addAttribute("producto", producto.get());
            return "productos/formulario";
        }
        return "redirect:/productos";
    }
    
    @GetMapping("/toggle/{id}")
    public String toggleEstadoProducto(@PathVariable Long id, RedirectAttributes attributes) {
        if (productoService.toggleProductStatus(id)) {
            attributes.addFlashAttribute("mensaje", "Estado del producto actualizado");
        } else {
            attributes.addFlashAttribute("error", "Producto no encontrado");
        }
        return "redirect:/productos";
    }
    
    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, RedirectAttributes attributes) {
        productoService.deleteProducto(id);
        attributes.addFlashAttribute("mensaje", "Producto eliminado correctamente");
        return "redirect:/productos";
    }
    
    @GetMapping("/detalle/{id}")
    public String verDetalleProducto(@PathVariable Long id, Model model) {
        Optional<Producto> producto = productoService.findProductoById(id);
        if (producto.isPresent()) {
            model.addAttribute("producto", producto.get());
            return "productos/detalle";
        }
        return "redirect:/productos";
    }
}