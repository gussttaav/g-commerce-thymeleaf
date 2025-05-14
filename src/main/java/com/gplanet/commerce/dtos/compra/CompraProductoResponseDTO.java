package com.gplanet.commerce.dtos.compra;

import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) for product purchase details.
 * This class represents detailed information about each product
 * in a purchase, including quantity, price, and subtotal calculations.
 *
 * @author Gustavo
 * @version 1.0
 *
 * @param id              The unique identifier of the product.
 * @param productoNombre  The name of the purchased product.
 * @param precioUnitario  The unit price of the product at the time of purchase.
 * @param cantidad        The quantity of the product purchased.
 * @param subtotal        The subtotal for this product (unit price * quantity).
 */
public record CompraProductoResponseDTO(
  Long id,
  String productoNombre,
  BigDecimal precioUnitario,
  Integer cantidad,
  BigDecimal subtotal
) {}