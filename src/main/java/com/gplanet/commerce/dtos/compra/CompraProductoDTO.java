package com.gplanet.commerce.dtos.compra;

import jakarta.validation.constraints.Positive;

/**
 * Data Transfer Object (DTO) for product purchase requests.
 * This class represents the information needed to purchase a specific
 * product, including the product identifier and the desired quantity.
 *
 * @author Gustavo
 * @version 1.0
 *
 * @param productoId  The ID of the product to be purchased. Must be a positive number.
 * @param cantidad    The quantity of the product to purchase. Must be a positive number.
 */
public record CompraProductoDTO(
  @Positive(message = "El ID del producto debe ser válido")
  Long productoId,

  @Positive(message = "La cantidad debe ser mayor a 0")
  Integer cantidad
) {}
