package com.gplanet.commerce.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gplanet.commerce.dtos.producto.ProductStatus;
import com.gplanet.commerce.dtos.producto.ProductoDTO;
import com.gplanet.commerce.dtos.producto.ProductoResponseDTO;
import com.gplanet.commerce.exceptions.ResourceNotFoundException;
import com.gplanet.commerce.services.ProductoService;
import com.gplanet.commerce.utilities.ToastUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@Controller
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping("/admin/listar")
    public String listarProductos(Model model) {
        List<ProductoResponseDTO> productos = productoService.listarProductos(ProductStatus.ALL);
        model.addAttribute("productos", productos);
        model.addAttribute("activePage", "adminProductos");
        return "productos/lista-admin";
    }

    @PostMapping("/admin/toggle-status/{id}")
    public String toggleStatus(@PathVariable Long id, Model model){
        try {
            ProductoResponseDTO updatedProduct = productoService.toggleProductStatus(id);
            model.addAttribute("producto", updatedProduct);
            ToastUtil.success(model, "Product status updated successfully.");
            return "productos/lista-admin-row :: producto-row";
        } catch (ResourceNotFoundException e) {
            ToastUtil.error(model, "Product not found.");
            return "empty :: empty";
        } catch (Exception e) {
            ToastUtil.error(model, "An error occurred while updating the product status.");
            return "empty :: empty";
        }
    }

    @GetMapping("/{id}")
    public String getProductById(@PathVariable Long id, Model model) {
        try {
            ProductoResponseDTO producto = productoService.findById(id);
            model.addAttribute("producto", producto);
            return "productos/producto-modal :: producto-modal";
        } catch (ResourceNotFoundException e) {
            ToastUtil.error(model, "Product not found.");
            return "empty :: empty";
        } catch (Exception e) {
            ToastUtil.error(model, "An error occurred while retrieving the product.");
            return "empty :: empty";
        }
    }

    @GetMapping("/admin/crear")
    public String showAddProductModal(Model model) {
        return "productos/producto-modal :: producto-modal";
    }

    @PostMapping("/admin/crear")
    public String crearProducto(@Valid ProductoDTO productoDTO, Model model) {
        try {
            ProductoResponseDTO createdProduct = productoService.crearProducto(productoDTO);
            model.addAttribute("producto", createdProduct);
            ToastUtil.success(model, "Product created successfully.");
            return "productos/lista-admin-row :: producto-row";
        } catch (Exception e) {
            ToastUtil.error(model, "Error creating product! Please try again.");
            return "empty :: empty";
        }
    }

    @PostMapping("/admin/actualizar/{id}")
    public String actualizarProducto(
                @PathVariable Long id, 
                @Valid ProductoDTO productoDTO,
                Model model) {
        try {
            ProductoResponseDTO updatedProduct = productoService.actualizarProducto(id, productoDTO);
            model.addAttribute("producto", updatedProduct);
            ToastUtil.success(model, "Product updated successfully.");
            return "productos/lista-admin-row :: producto-row";
        } catch (ResourceNotFoundException e) {
            ToastUtil.error(model, "Product not found.");
            return "empty :: empty";
        } catch (Exception e) {
            ToastUtil.error(model, "Error updating product! Please try again.");
            return "empty :: empty";
        }
    }
}