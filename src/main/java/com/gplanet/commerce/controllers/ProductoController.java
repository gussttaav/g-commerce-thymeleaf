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
import com.gplanet.commerce.exceptions.ProductCreationException;
import com.gplanet.commerce.exceptions.ResourceNotFoundException;
import com.gplanet.commerce.services.ProductoService;
import com.gplanet.commerce.utilities.ToastUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller class that handles product-related operations.
 * Provides endpoints for CRUD operations on products and product listing.
 * 
 * @author Gustavo
 * @version 1.0
 */
@Controller
@RequestMapping("/productos")
@RequiredArgsConstructor
@Slf4j
public class ProductoController {

  private final ProductoService productoService;

  /**
   * Lists all products with pagination support (admin view).
   * 
   * @param model Spring MVC model
   * @return View name for product list page
   */
  @GetMapping("/admin/listar")
  public String listarProductos(Model model) {
    if (log.isDebugEnabled()) {
      log.debug("Listing all products (admin view)");
    }
    Page<ProductoResponseDTO> productosPage = productoService.listarProductos(
        ProductStatus.ALL, "", 0, 10, "nombre", "ASC");
    PaginatedResponse<ProductoResponseDTO> paginatedResponse = PaginatedResponse.fromPage(productosPage);

    model.addAttribute("productos", productosPage.getContent());
    model.addAttribute("pagination", paginatedResponse);
    model.addAttribute("activePage", "adminProductos");

    return "productos/lista-admin";
  }

  /**
   * Toggles the active status of a product.
   * 
   * @param id    ID of the product to toggle
   * @param model Spring MVC model
   * @return View name or fragment containing updated product status
   */
  @PostMapping("/admin/toggle-status/{id}")
  public String toggleStatus(@PathVariable Long id, Model model) {
    if (log.isDebugEnabled()) {
      log.debug("Attempting to toggle status for product ID: {}", id);
    }
    try {
      ProductoResponseDTO updatedProduct = productoService.toggleProductStatus(id);
      log.info("Product status successfully toggled for ID: {}", id);
      model.addAttribute("producto", updatedProduct);
      ToastUtil.success(model, "Product status updated successfully.");
      return "productos/lista-admin-row :: producto-row";
    } catch (ResourceNotFoundException e) {
      log.error("Error toggling product status - Product not found with ID: {}", id, e);
      ToastUtil.error(model, "Product not found.");
      return "empty :: empty";
    }
  }

  /**
   * Retrieves product details by ID.
   * 
   * @param id    ID of the product to retrieve
   * @param model Spring MVC model
   * @return View name for product details page
   */
  @GetMapping("/{id}")
  public String getProductById(@PathVariable Long id, Model model) {
    if (log.isDebugEnabled()) {
      log.debug("Retrieving product details for ID: {}", id);
    }
    try {
      ProductoResponseDTO producto = productoService.findById(id);
      model.addAttribute("producto", producto);
      return "productos/producto-modal :: producto-modal";
    } catch (ResourceNotFoundException e) {
      log.error("Error retrieving product - Product not found with ID: {}", id, e);
      ToastUtil.error(model, "Product not found.");
      return "empty :: empty";
    }
  }

  /**
   * Filters products for regular users with pagination support.
   * 
   * @param page      Page number (zero-based)
   * @param size      Items per page
   * @param search    Text to search in product name and description
   * @param sort      Field to sort by
   * @param direction Sort direction (ASC or DESC)
   * @param model     Spring MVC model
   * @return Fragment name containing filtered product grid
   */
  @GetMapping("/filtrar")
  public String filterProducts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "") String search,
      @RequestParam(defaultValue = "nombre") String sort,
      @RequestParam(defaultValue = "ASC") String direction,
      Model model) {
    if (log.isDebugEnabled()) {
      log.debug("Filtering products - page: {}, size: {}, search: {}, sort: {} {}",
          page, size, search, sort, direction);
    }

    Page<ProductoResponseDTO> productosPage = productoService.listarProductos(
        ProductStatus.ACTIVE, search, page, size, sort, direction);

    PaginatedResponse<ProductoResponseDTO> paginatedResponse = PaginatedResponse.fromPage(productosPage);

    model.addAttribute("productos", productosPage.getContent());
    model.addAttribute("pagination", paginatedResponse);

    return "productos/user-grid :: user-grid";
  }

  /**
   * Filters products for admin view with pagination and status filtering support.
   * 
   * @param status    Product status filter (ALL, ACTIVE, or INACTIVE)
   * @param page      Page number (zero-based)
   * @param size      Items per page
   * @param search    Text to search in product name and description
   * @param sort      Field to sort by
   * @param direction Sort direction (ASC or DESC)
   * @param model     Spring MVC model
   * @return Fragment name containing filtered product table
   */
  @GetMapping("/admin/filtrar")
  public String filterAdminProducts(
      @RequestParam(defaultValue = "ALL") ProductStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "") String search,
      @RequestParam(defaultValue = "nombre") String sort,
      @RequestParam(defaultValue = "ASC") String direction,
      Model model) {
    if (log.isDebugEnabled()) {
      log.debug("Filtering admin products - status: {}, page: {}, size: {}, search: {}, sort: {} {}",
          status, page, size, search, sort, direction);
    }

    Page<ProductoResponseDTO> productosPage = productoService.listarProductos(
        status, search, page, size, sort, direction);

    PaginatedResponse<ProductoResponseDTO> paginatedResponse = PaginatedResponse.fromPage(productosPage);

    model.addAttribute("productos", productosPage.getContent());
    model.addAttribute("pagination", paginatedResponse);

    return "productos/lista-admin-page :: producto-page";
  }

  /**
   * Shows the modal form for adding a new product.
   * 
   * @param model Spring MVC model
   * @return Fragment name containing product modal form
   */
  @GetMapping("/admin/crear")
  public String showAddProductModal(Model model) {
    if (log.isDebugEnabled()) {
      log.debug("Showing modal for new product creation");
    }
    return "productos/producto-modal :: producto-modal";
  }

  /**
   * Creates a new product.
   * 
   * @param productoDTO Data transfer object containing new product information
   * @param model       Spring MVC model
   * @return Fragment name containing new product row or error message
   * @throws Exception if there's an error during product creation
   */
  @PostMapping("/admin/crear")
  public String crearProducto(@Valid ProductoDTO productoDTO, Model model) {
    try {
      if (log.isDebugEnabled()) {
        log.debug("Attempting to create new product: {}", productoDTO.nombre());
      }
      ProductoResponseDTO createdProduct = productoService.crearProducto(productoDTO);
      log.info("Product successfully created: {}", productoDTO.nombre());
      model.addAttribute("producto", createdProduct);
      ToastUtil.success(model, "Product created successfully.");
      return "productos/lista-admin-row :: producto-row";
    } catch (ProductCreationException e) {
      log.error("Product creation failed", e);
      ToastUtil.error(model, e.getMessage());
      return "empty :: empty";
    }
  }

  /**
   * Updates an existing product's information.
   * 
   * @param id          ID of the product to update
   * @param productoDTO Data transfer object containing updated product
   *                    information
   * @param model       Spring MVC model
   * @return Fragment name containing updated product row or error message
   * @throws ResourceNotFoundException if the product is not found
   * @throws Exception                 if there's an error during product update
   */
  @PostMapping("/admin/actualizar/{id}")
  public String actualizarProducto(
      @PathVariable Long id,
      @Valid ProductoDTO productoDTO,
      Model model) {
    if (log.isDebugEnabled()) {
      log.debug("Attempting to update product ID: {} with name: {}", id, productoDTO.nombre());
    }
    try {
      ProductoResponseDTO updatedProduct = productoService.actualizarProducto(id, productoDTO);
      log.info("Product successfully updated - ID: {}, Name: {}", id, productoDTO.nombre());
      model.addAttribute("producto", updatedProduct);
      ToastUtil.success(model, "Product updated successfully.");
      return "productos/lista-admin-row :: producto-row";
    } catch (ResourceNotFoundException e) {
      log.error("Error updating product - Product not found with ID: {}", id, e);
      ToastUtil.error(model, "Product not found.");
      return "empty :: empty";
    }
  }
}