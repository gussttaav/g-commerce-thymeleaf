package com.gplanet.commerce.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
import com.gplanet.commerce.exceptions.ResourceNotFoundException;
import com.gplanet.commerce.repositories.ProductoRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoService {
    
    private final ProductoMapper productoMapper;
    private final ProductoRepository productoRepository;
    
    /**
     * Lists and searches products based on the specified status and search text with pagination support.
     * Search is performed on both product name and description fields.
     * 
     * @param status The status to filter products by
     * @param searchText Optional text to search within product name and description (case-insensitive)
     * @param page The page number (zero-based)
     * @param size The page size
     * @param sort The field to sort by
     * @param direction The sort direction (ASC or DESC)
     * @return Page of ProductoResponseDTO containing filtered and searched paginated products
     */
    public Page<ProductoResponseDTO> listarProductos(
            ProductStatus status, 
            String searchText, 
            int page, 
            int size, 
            String sort, 
            String direction) {
        
        log.debug("Listing products with status: {}, search: '{}' and pagination - page: {}, size: {}, sort: {}, direction: {}", 
                status, searchText, page, size, sort, direction);
        
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

    @Transactional
    public ProductoResponseDTO crearProducto(ProductoDTO productoDTO) {
        log.info("Creating new product: {}", productoDTO.getNombre());
        Producto producto = productoMapper.toProducto(productoDTO);
        producto.setActivo(true);
        producto.setFechaCreacion(LocalDateTime.now());
        
        Producto savedProducto = productoRepository.save(producto);
        log.info("Product created with ID: {}", savedProducto.getId());
        return productoMapper.toProductoResponseDTO(savedProducto);
    }


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

    public ProductoResponseDTO findById(Long id) {
        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        
        return productoMapper.toProductoResponseDTO(producto);
    }
}