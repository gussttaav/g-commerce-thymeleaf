package com.gplanet.commerce.dtos.compra;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object (DTO) for purchase response information.
 * This class represents the complete purchase information including
 * customer details, purchase date, total amount, and the list of
 * purchased products.
 *
 * @author Gustavo
 * @version 1.0
 *
 * @param id              The unique identifier of the purchase.
 * @param usuarioNombre   The name of the customer who made the purchase.
 * @param fecha           The date and time when the purchase was made.
 * @param total           The total amount of the purchase.
 * @param productos       The list of products included in this purchase.
 */
public record CompraResponseDTO(
    Long id,
    String usuarioNombre,
    LocalDateTime fecha,
    BigDecimal total,
    List<CompraProductoResponseDTO> productos
) {}
