package com.gplanet.commerce.dtos;

import java.math.BigDecimal;
import lombok.Data;

/**
 * Data Transfer Object (DTO) for product purchase details.
 * This class represents detailed information about each product
 * in a purchase, including quantity, price, and subtotal calculations.
 *
 * @author Gustavo
 * @version 1.0
 */
@Data
public class CompraProductoResponseDTO {
    /**
     * The unique identifier of the product.
     */
    private Long id;

    /**
     * The name of the purchased product.
     */
    private String productoNombre;

    /**
     * The unit price of the product at the time of purchase.
     */
    private BigDecimal precioUnitario;

    /**
     * The quantity of the product purchased.
     */
    private Integer cantidad;

    /**
     * The subtotal for this product (unit price * quantity).
     */
    private BigDecimal subtotal;
}