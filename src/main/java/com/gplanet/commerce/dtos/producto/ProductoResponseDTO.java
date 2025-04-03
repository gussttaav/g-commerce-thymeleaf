package com.gplanet.commerce.dtos.producto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) for product responses.
 * This class represents the product data that is sent back to templates
 * containing all relevant product information.
 *
 * @author Gustavo
 * @version 1.0
 */
@Data
@AllArgsConstructor
@Builder
public class ProductoResponseDTO {
    /**
     * The unique identifier of the product.
     */
    private Long id;

    /**
     * The name of the product.
     */
    private String nombre;

    /**
     * The detailed description of the product.
     */
    private String descripcion;

    /**
     * The price of the product.
     */
    private BigDecimal precio;

    /**
     * The timestamp when the product was created.
     */
    private LocalDateTime fechaCreacion;

    /**
     * Indicates whether the product is currently active in the system.
     */
    private boolean activo;
}