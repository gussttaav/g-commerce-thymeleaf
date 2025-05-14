package com.gplanet.commerce.entities;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Entity class representing a purchase in the system.
 * Each purchase is associated with a user and contains one or more products.
 *
 * @author Gustavo
 * @version 1.0
 */
@Getter
@Setter
@Entity
@Table(name = "compras")
public class Compra {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  /**
   * The user who made the purchase.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id")
  private Usuario usuario;
  
  /**
   * Timestamp when the purchase was made.
   */
  private LocalDateTime fecha;

  /**
   * Total amount of the purchase.
   */
  private BigDecimal total;
  
  /**
   * List of products included in this purchase.
   */
  @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CompraProducto> productos = new ArrayList<>();

  /**
   * Adds a product to the purchase and maintains the bidirectional relationship.
   * 
   * @param compraProducto The product entry to add to the purchase
   */
  public void addCompraProducto(CompraProducto compraProducto) {
    productos.add(compraProducto);
    compraProducto.setCompra(this);
  }
}
