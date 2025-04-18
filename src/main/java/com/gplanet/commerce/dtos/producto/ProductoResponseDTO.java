package com.gplanet.commerce.dtos.producto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) for product responses.
 * Represents the product data sent in responses, including all relevant details.
 *
 * @param id             The unique identifier of the product.
 * @param nombre         The name of the product.
 * @param descripcion    The detailed description of the product.
 * @param precio         The price of the product.
 * @param fechaCreacion  The timestamp when the product was created.
 * @param activo         Indicates whether the product is currently active in the system.
 * 
 * @author Gustavo
 * @version 1.0
 */
public record ProductoResponseDTO(
    Long id,
    String nombre,
    String descripcion,
    BigDecimal precio,
    LocalDateTime fechaCreacion,
    boolean activo
) {}