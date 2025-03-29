package com.gplanet.commerce.dtos;

import org.springframework.stereotype.Component;

import com.gplanet.commerce.entities.Compra;
import com.gplanet.commerce.entities.CompraProducto;

import java.util.stream.Collectors;

/**
 * Mapper class responsible for converting between Compra entities and DTOs.
 * This class provides methods to transform purchase-related entities into
 * their corresponding DTO representations for API responses.
 *
 * @author Gustavo
 * @version 1.0
 */
@Component
public class CompraMapper {
    
    /**
     * Converts a Compra entity to its response DTO representation.
     * This method maps all the purchase information including the list
     * of purchased products to their DTO representations.
     *
     * @param entity the purchase entity to convert
     * @return the corresponding CompraResponseDTO with all purchase information
     */
    public CompraResponseDTO toCompraResponseDTO(Compra entity) {
        CompraResponseDTO dto = new CompraResponseDTO();
        dto.setId(entity.getId());
        dto.setUsuarioNombre(entity.getUsuario().getNombre());
        dto.setFecha(entity.getFecha());
        dto.setTotal(entity.getTotal());
        dto.setProductos(entity.getProductos().stream()
            .map(this::toCompraProductoResponseDTO)
            .collect(Collectors.toList()));
        return dto;
    }

    /**
     * Converts a CompraProducto entity to its response DTO representation.
     * This method maps the product details, quantity, and pricing information
     * to the corresponding DTO fields.
     *
     * @param entity the purchase product entity to convert
     * @return the corresponding CompraProductoResponseDTO
     */
    private CompraProductoResponseDTO toCompraProductoResponseDTO(CompraProducto entity) {
        CompraProductoResponseDTO dto = new CompraProductoResponseDTO();
        dto.setId(entity.getProducto().getId());
        dto.setProductoNombre(entity.getProducto().getNombre());
        dto.setPrecioUnitario(entity.getProducto().getPrecio());
        dto.setCantidad(entity.getCantidad());
        dto.setSubtotal(entity.getSubtotal());
        return dto;
    }
}
