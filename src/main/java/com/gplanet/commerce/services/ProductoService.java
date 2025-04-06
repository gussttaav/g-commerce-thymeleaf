package com.gplanet.commerce.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gplanet.commerce.dtos.producto.ProductStatus;
import com.gplanet.commerce.dtos.producto.ProductoDTO;
import com.gplanet.commerce.dtos.producto.ProductoMapper;
import com.gplanet.commerce.dtos.producto.ProductoResponseDTO;
import com.gplanet.commerce.entities.Producto;
import com.gplanet.commerce.exceptions.ResourceNotFoundException;
import com.gplanet.commerce.repositories.ProductoRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoService {
    
    private final ProductoMapper productoMapper;
    private final ProductoRepository productoRepository;
    
    public List<ProductoResponseDTO> listarProductos(ProductStatus status) {
        
        log.debug("Listing products with status: {}", status);
        
        List<Producto> productos = switch (status) {
            case ACTIVE -> productoRepository.findByActivoTrue();
            case INACTIVE -> productoRepository.findByActivoFalse();
            case ALL -> productoRepository.findAll();
        };
        
        // Map to DTOs
        return productos.stream()
                        .map(productoMapper::toProductoResponseDTO)
                        .toList();
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