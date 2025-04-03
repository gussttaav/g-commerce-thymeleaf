package com.gplanet.commerce.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gplanet.commerce.dtos.compra.CompraDTO;
import com.gplanet.commerce.dtos.compra.CompraMapper;
import com.gplanet.commerce.dtos.compra.CompraProductoDTO;
import com.gplanet.commerce.dtos.compra.CompraResponseDTO;
import com.gplanet.commerce.entities.Compra;
import com.gplanet.commerce.entities.CompraProducto;
import com.gplanet.commerce.entities.Producto;
import com.gplanet.commerce.entities.Usuario;
import com.gplanet.commerce.exceptions.ResourceNotFoundException;
import com.gplanet.commerce.repositories.CompraRepository;
import com.gplanet.commerce.repositories.ProductoRepository;
import com.gplanet.commerce.repositories.UsuarioRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class that handles purchase-related operations including
 * creating new purchases and listing purchase history.
 * 
 * @author Gustavo
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompraService {
    private final CompraMapper compraMapper;
    private final CompraRepository compraRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Lists purchases based on user role with pagination support.
     * Admins see all purchases, regular users see only their own purchases.
     * 
     * @param email Email of the requesting user
     * @param page The page number (zero-based)
     * @param size The page size
     * @param sort The field to sort by
     * @param direction The sort direction (ASC or DESC)
     * @throws UsernameNotFoundException if user is not found
     */
    @Transactional(readOnly = true)
    public List<CompraResponseDTO> listarCompras(String email) {
        log.debug("Listing purchases for user: {}", 
                email);
        
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
            
        // Get paginated result based on user role
        List<Compra> compras;
        if (usuario.getRol() == Usuario.Role.ADMIN) {
            compras = compraRepository.findAll();
        } else {
            compras = compraRepository.findByUsuario(usuario);
        }
        
        // Map to DTOs
        List<CompraResponseDTO> result = compras.stream()
            .map(compraMapper::toCompraResponseDTO)
            .collect(Collectors.toList());
        
        log.debug("Found {} purchases", result.size());
                
        return result;
    }

    /**
     * Processes a new purchase for a user, calculating totals and
     * creating all necessary purchase records.
     * 
     * @param email Email of the user making the purchase
     * @param compraDTO Data transfer object containing purchase information
     * @return CompraResponseDTO containing the created purchase information
     * @throws UsernameNotFoundException if user is not found
     * @throws ResourceNotFoundException if any product in the purchase is not found
     */
    @Transactional
    public void realizarCompra(String email, CompraDTO compraDTO) {
        log.info("Starting new purchase for user: {}", email);
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        Compra compra = new Compra();
        compra.setUsuario(usuario);
        compra.setFecha(LocalDateTime.now());
        compra.setTotal(BigDecimal.ZERO);

        BigDecimal total = BigDecimal.ZERO;

        for (CompraProductoDTO item : compraDTO.getProductos()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

            CompraProducto compraProducto = new CompraProducto();
            compraProducto.setProducto(producto);
            compraProducto.setCantidad(item.getCantidad());
            compraProducto.setSubtotal(
                producto.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad()))
            );
            
            compra.addCompraProducto(compraProducto);
            total = total.add(compraProducto.getSubtotal());
        }

        compra.setTotal(total);        
        Compra savedCompra = compraRepository.save(compra);
        
        log.info("Purchase completed - ID: {}, Total: {}", savedCompra.getId(), savedCompra.getTotal());
    }
}