package com.gplanet.commerce.dtos.compra;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Data Transfer Object (DTO) for purchase requests.
 * This class represents the structure of data received when a client
 * submits a new purchase order, containing a list of products to be purchased.
 *
 * @author Gustavo
 * @version 1.0
 *
 * @param productos  The list of products to be purchased.
 *                   Must contain at least one product.
 */
public record CompraDTO(
  @NotEmpty(message = "Debe incluir al menos un producto")
  @Valid
  List<CompraProductoDTO> productos
) {}