package com.gplanet.commerce.controllers;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gplanet.commerce.dtos.pagination.PaginatedResponse;
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
        Page<ProductoResponseDTO> productosPage = productoService.listarProductos(
            ProductStatus.ALL, "", 0, 10, "nombre", "ASC");
        PaginatedResponse<ProductoResponseDTO> paginatedResponse = PaginatedResponse.fromPage(productosPage);

        model.addAttribute("productos", productosPage.getContent());
        model.addAttribute("pagination", paginatedResponse);
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

    @PostMapping("/filtrar")
    public String filterProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "nombre") String sort,
            @RequestParam(defaultValue = "ASC") String direction,
            Model model) {
        
        // Get filtered and paginated products
        Page<ProductoResponseDTO> productosPage = productoService.listarProductos(
            ProductStatus.ACTIVE, search, page, size, sort, direction);
        
        PaginatedResponse<ProductoResponseDTO> paginatedResponse = PaginatedResponse.fromPage(productosPage);
        
        model.addAttribute("productos", productosPage.getContent());
        model.addAttribute("pagination", paginatedResponse);
        
        return "productos/user-grid :: user-grid";
    }

    @PostMapping("/admin/filtrar")
    public String filterAdminProducts(
            @RequestParam(defaultValue = "ALL") ProductStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "nombre") String sort,
            @RequestParam(defaultValue = "ASC") String direction,
            Model model) {
        
        // Get filtered and paginated products
        Page<ProductoResponseDTO> productosPage = productoService.listarProductos(
            status, search, page, size, sort, direction);
        
        PaginatedResponse<ProductoResponseDTO> paginatedResponse = PaginatedResponse.fromPage(productosPage);
        
        model.addAttribute("productos", productosPage.getContent());
        model.addAttribute("pagination", paginatedResponse);
        
        return "productos/lista-admin-page :: producto-page";
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