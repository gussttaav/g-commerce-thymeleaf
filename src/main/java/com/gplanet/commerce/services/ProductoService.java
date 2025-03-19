package com.gplanet.commerce.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gplanet.commerce.entities.Producto;
import com.gplanet.commerce.repositories.ProductoRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductoService {
    
    private final ProductoRepository productoRepository;
    
    @Transactional(readOnly = true)
    public List<Producto> findAllProductos() {
        return productoRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<Producto> findAllActiveProductos() {
        return productoRepository.findByActivoTrue();
    }
    
    @Transactional(readOnly = true)
    public Optional<Producto> findProductoById(Long id) {
        return productoRepository.findById(id);
    }
    
    @Transactional
    public Producto saveProducto(Producto producto) {
        return productoRepository.save(producto);
    }
    
    @Transactional
    public void deleteProducto(Long id) {
        productoRepository.deleteById(id);
    }
    
    @Transactional
    public boolean toggleProductStatus(Long id) {
        Optional<Producto> optionalProducto = productoRepository.findById(id);
        if (optionalProducto.isPresent()) {
            Producto producto = optionalProducto.get();
            producto.setActivo(!producto.isActivo());
            productoRepository.save(producto);
            return true;
        }
        return false;
    }
}