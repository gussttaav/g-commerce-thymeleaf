package com.gplanet.commerce.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.gplanet.commerce.entities.Producto;

@DataJpaTest
@ActiveProfiles("test")
class ProductoRepositoryTest {
    @Autowired
    private ProductoRepository productoRepository;
    
    @Test
    void save_ValidProduct_Success() {
        // Arrange
        Producto producto = new Producto();
        producto.setNombre("Test Product");
        producto.setDescripcion("Test Description");
        producto.setPrecio(new BigDecimal("99.99"));
        producto.setActivo(true);
        producto.setFechaCreacion(LocalDateTime.now());
        
        // Act
        Producto savedProducto = productoRepository.save(producto);
        
        // Assert
        assertNotNull(savedProducto.getId());
        assertEquals("Test Product", savedProducto.getNombre());
        assertEquals(new BigDecimal("99.99"), savedProducto.getPrecio());
    }
    
    @Test
    void save_DuplicateProductName_ThrowsException() {
        // Arrange
        Producto producto1 = new Producto();
        producto1.setNombre("Test Product");
        producto1.setPrecio(new BigDecimal("99.99"));
        producto1.setActivo(true);
        productoRepository.save(producto1);
        
        Producto producto2 = new Producto();
        producto2.setNombre("Test Product"); // Same name
        producto2.setPrecio(new BigDecimal("149.99"));
        producto2.setActivo(true);
        
        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> {
            productoRepository.save(producto2);
            productoRepository.flush(); // Force the persistence
        });
    }
    
    @Test
    void findByActivo_ActiveProducts_ReturnsOnlyActive() {
        // Arrange
        Producto activeProduct = new Producto();
        activeProduct.setNombre("Active Product");
        activeProduct.setPrecio(new BigDecimal("99.99"));
        activeProduct.setActivo(true);
        productoRepository.save(activeProduct);
        
        Producto inactiveProduct = new Producto();
        inactiveProduct.setNombre("Inactive Product");
        inactiveProduct.setPrecio(new BigDecimal("79.99"));
        inactiveProduct.setActivo(false);
        productoRepository.save(inactiveProduct);
        
        // Act
        Page<Producto> activeProducts = productoRepository.findByActivoTrue(PageRequest.of(0, 10));
        
        // Assert
        assertEquals(1, activeProducts.getTotalElements());
        assertTrue(activeProducts.getContent().get(0).isActivo());
        assertEquals("Active Product", activeProducts.getContent().get(0).getNombre());
    }
}