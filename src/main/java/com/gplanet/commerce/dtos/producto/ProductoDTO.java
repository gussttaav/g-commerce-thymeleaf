package com.gplanet.commerce.dtos.producto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) for creating and updating products.
 * Defines the structure of product data received from clients
 * when creating or updating products in the system.
 *
 * @param nombre       The name of the product. This field is required.
 * @param descripcion  The detailed description of the product. Optional field.
 * @param precio       The price of the product. Must be a positive number.
 * @param activo       Indicates if the product is active. Defaults to true.
 * 
 * @author Gustavo
 * @version 1.0
 */
public record ProductoDTO(
  @NotBlank(message = "The name is mandatory")
  String nombre,

  String descripcion,

  @Positive(message = "The price must be greater than 0")
  BigDecimal precio,

  boolean activo
) {
  /**
   * Constructs a ProductoDTO with the 'activo' field defaulting to true if not provided.
   */
  public ProductoDTO(String nombre, String descripcion, BigDecimal precio) {
      this(nombre, descripcion, precio, true);
  }
}