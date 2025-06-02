package com.gplanet.commerce.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.gplanet.commerce.dtos.producto.ProductStatus;
import com.gplanet.commerce.dtos.producto.ProductoDTO;
import com.gplanet.commerce.dtos.producto.ProductoMapper;
import com.gplanet.commerce.dtos.producto.ProductoResponseDTO;
import com.gplanet.commerce.entities.Producto;
import com.gplanet.commerce.exceptions.ProductCreationException;
import com.gplanet.commerce.exceptions.ResourceNotFoundException;
import com.gplanet.commerce.repositories.ProductoRepository;

import java.time.LocalDateTime;

/**
 * Service class that handles product-related operations including creation,
 * updating, listing, and status management of products.
 * 
 * @author Gustavo
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoService {

  private final ProductoMapper productoMapper;
  private final ProductoRepository productoRepository;

  /**
   * Lists and searches products based on the specified status and search text
   * with pagination support.
   * Search is performed on both product name and description fields.
   * 
   * @param status     The status to filter products by
   * @param searchText Optional text to search within product name and description
   *                   (case-insensitive)
   * @param page       The page number (zero-based)
   * @param size       The page size
   * @param sort       The field to sort by
   * @param direction  The sort direction (ASC or DESC)
   * @return Page of ProductoResponseDTO containing filtered and searched
   *         paginated products
   */
  public Page<ProductoResponseDTO> listarProductos(
      ProductStatus status,
      String searchText,
      int page,
      int size,
      String sort,
      String direction) {

    if (log.isDebugEnabled()) {
      log.debug(
          "Listing products with status: {}, search: '{}' " +
              "and pagination - page: {}, size: {}, sort: {}, direction: {}",
          status, searchText, page, size, sort, direction);
    }

    Sort.Direction sortDirection = Sort.Direction.fromString(direction);
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

    Page<Producto> productosPage;

    if (StringUtils.hasText(searchText)) {
      String searchTerm = "%" + searchText.toLowerCase() + "%";

      // Get paginated results based on status and search text
      switch (status) {
        case ACTIVE:
          productosPage = productoRepository.findByActivoTrueAndSearch(searchTerm, pageable);
          break;
        case INACTIVE:
          productosPage = productoRepository.findByActivoFalseAndSearch(searchTerm, pageable);
          break;
        case ALL:
          productosPage = productoRepository.findBySearch(searchTerm, pageable);
          break;
        default:
          productosPage = Page.empty(pageable);
      }
    } else {
      // Original code for when no search is performed
      productosPage = switch (status) {
        case ACTIVE -> productoRepository.findByActivoTrue(pageable);
        case INACTIVE -> productoRepository.findByActivoFalse(pageable);
        case ALL -> productoRepository.findAll(pageable);
      };
    }

    // Map to DTOs
    return productosPage.map(productoMapper::toProductoResponseDTO);
  }

  /**
   * Toggles the active status of a product.
   * 
   * @param id The ID of the product to toggle
   * @return ProductoResponseDTO containing the updated product information
   * @throws ResourceNotFoundException if the product is not found
   */
  @Transactional
  public ProductoResponseDTO toggleProductStatus(Long id) {
    log.info("Updating product status with ID: {}", id);
    Producto producto = productoRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found!"));

    producto.setActivo(!producto.isActivo());
    Producto updatedProduct = productoRepository.save(producto);

    log.info("Status of the products successfully changed - ID: {}", id);
    return productoMapper.toProductoResponseDTO(updatedProduct);
  }

  /**
   * Creates a new product in the system.
   * 
   * @param productoDTO Data transfer object containing product information
   * @return ProductoResponseDTO containing the created product information
   * @throws ProductCreationException if the product cannot be created due
   *                                  to a data access issue or other unexpected
   *                                  error
   */
  @Transactional
  public ProductoResponseDTO crearProducto(ProductoDTO productoDTO) throws ProductCreationException {
    try {
      log.info("Creating new product: {}", productoDTO.nombre());
      Producto producto = productoMapper.toProducto(productoDTO);
      producto.setActivo(true);
      producto.setFechaCreacion(LocalDateTime.now());

      Producto savedProducto = productoRepository.save(producto);
      log.info("Product created with ID: {}", savedProducto.getId());
      return productoMapper.toProductoResponseDTO(savedProducto);
    } catch (DataAccessException e) {
      throw new ProductCreationException("Failed to create product due to data access error");
    } catch (Exception e) {
      throw new ProductCreationException("Unexpected error creating product");
    }
  }

  /**
   * Updates an existing product's information.
   * 
   * @param id          The ID of the product to update
   * @param productoDTO Data transfer object containing the new product
   *                    information
   * @return ProductoResponseDTO containing the updated product information
   * @throws ResourceNotFoundException if the product is not found
   */
  @Transactional
  public ProductoResponseDTO actualizarProducto(Long id, ProductoDTO productoDTO) {
    log.info("Updating product with ID: {}", id);
    Producto producto = productoRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

    productoMapper.updateProductoFromDTO(productoDTO, producto);
    Producto updatedProducto = productoRepository.save(producto);

    log.info("Product successfully updated - ID: {}", updatedProducto.getId());
    return productoMapper.toProductoResponseDTO(updatedProducto);
  }

  /**
   * Retrieves a product by its ID.
   * 
   * @param id The ID of the product to find
   * @return ProductoResponseDTO containing the product information
   * @throws ResourceNotFoundException if the product is not found
   */
  public ProductoResponseDTO findById(Long id) {
    Producto producto = productoRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

    return productoMapper.toProductoResponseDTO(producto);
  }
}