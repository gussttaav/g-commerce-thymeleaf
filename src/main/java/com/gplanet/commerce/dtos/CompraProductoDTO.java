package com.gplanet.commerce.dtos;

import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Data Transfer Object (DTO) for product purchase requests.
 * This class represents the information needed to purchase a specific
 * product, including the product identifier and the desired quantity.
 *
 * @author Gustavo
 * @version 1.0
 */
@Data
public class CompraProductoDTO {
    /**
     * The ID of the product to be purchased.
     * Must be a positive number.
     */
    @Positive(message = "El ID del producto debe ser válido")
    private Long productoId;
    
    /**
     * The quantity of the product to purchase.
     * Must be a positive number.
     */
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;
}
