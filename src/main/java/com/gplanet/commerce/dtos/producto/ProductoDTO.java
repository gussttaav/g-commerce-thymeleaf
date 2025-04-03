package com.gplanet.commerce.dtos.producto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) for creating and updating products.
 * This class defines the structure of product data received from clients
 * when creating or updating products in the system.
 *
 * @author Gustavo
 * @version 1.0
 */
@Data
@AllArgsConstructor
@Builder
public class ProductoDTO {
    /**
     * The name of the product. This field is required.
     */
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    
    /**
     * The detailed description of the product. Optional field.
     */
    private String descripcion;
    
    /**
     * The price of the product. Must be a positive number.
     */
    @Positive(message = "El precio debe ser mayor a 0")
    private BigDecimal precio;
    
    /**
     * Indicates if the product is active. Defaults to true.
     */
    @Builder.Default
    private boolean activo = true;
}
