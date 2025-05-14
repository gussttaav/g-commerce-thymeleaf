package com.gplanet.commerce.entities;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity class representing a product in the system.
 * Products can be purchased by users and can be active or inactive.
 *
 * @author Gustavo
 * @version 1.0
 */
@Data
@Entity
@Table(name = "productos")
public class Producto {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  /**
   * The product name. Must be unique in the system.
   */
  @Column(unique = true)
  private String nombre;

  /**
   * Detailed description of the product.
   */
  private String descripcion;

  /**
   * The product's price.
   */
  private BigDecimal precio;
  
  /**
   * Timestamp when the product was created.
   */
  @Column(name = "fecha_creacion")
  private LocalDateTime fechaCreacion;
  
  /**
   * Flag indicating if the product is currently active and available for purchase.
   */
  private boolean activo;
}
