package com.gplanet.commerce.dtos.compra;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * Data Transfer Object (DTO) for purchase response information.
 * This class represents the complete purchase information including
 * customer details, purchase date, total amount, and the list of
 * purchased products.
 *
 * @author Gustavo
 * @version 1.0
 */
@Data
public class CompraResponseDTO {
    /**
     * The unique identifier of the purchase.
     */
    private Long id;

    /**
     * The name of the customer who made the purchase.
     */
    private String usuarioNombre;

    /**
     * The date and time when the purchase was made.
     */
    private LocalDateTime fecha;

    /**
     * The total amount of the purchase.
     */
    private BigDecimal total;

    /**
     * The list of products included in this purchase.
     */
    private List<CompraProductoResponseDTO> productos;
}
