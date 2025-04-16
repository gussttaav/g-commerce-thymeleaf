package com.gplanet.commerce.entities;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entity class representing a product entry in a purchase.
 * This is a join table between Purchase and Product that includes quantity and subtotal.
 *
 * @author Gustavo
 * @version 1.0
 */
@Getter
@Setter
@Entity
@Table(name = "compra_productos")
public class CompraProducto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * The purchase this entry belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id")
    private Compra compra;
    
    /**
     * The product included in the purchase
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;
    
    /**
     * Quantity of the product purchased
     */
    private Integer cantidad;

    /**
     * Subtotal for this product entry (price * quantity)
     */
    private BigDecimal subtotal;
}
