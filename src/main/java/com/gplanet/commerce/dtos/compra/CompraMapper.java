package com.gplanet.commerce.dtos.compra;

import org.springframework.stereotype.Component;

import com.gplanet.commerce.entities.Compra;
import com.gplanet.commerce.entities.CompraProducto;

import java.util.List;

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
   * Maps all purchase information including the list of purchased products
   * to their corresponding DTOs.
   *
   * @param entity the purchase entity to convert
   * @return the corresponding CompraResponseDTO with all purchase information
   */
  public CompraResponseDTO toCompraResponseDTO(Compra entity) {
      List<CompraProductoResponseDTO> productosDTO = entity.getProductos().stream()
          .map(this::toCompraProductoResponseDTO)
          .toList();

      return new CompraResponseDTO(
          entity.getId(),
          entity.getUsuario().getNombre(),
          entity.getFecha(),
          entity.getTotal(),
          productosDTO
      );
  }

  /**
   * Converts a CompraProducto entity to its response DTO representation.
   * Maps product details, quantity, and pricing info to the corresponding DTO.
   *
   * @param entity the purchase product entity to convert
   * @return the corresponding CompraProductoResponseDTO
   */
  private CompraProductoResponseDTO toCompraProductoResponseDTO(CompraProducto entity) {
      return new CompraProductoResponseDTO(
          entity.getProducto().getId(),
          entity.getProducto().getNombre(),
          entity.getProducto().getPrecio(),
          entity.getCantidad(),
          entity.getSubtotal()
      );
  }
}
