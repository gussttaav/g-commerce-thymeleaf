package com.gplanet.commerce.repositories;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gplanet.commerce.entities.Compra;
import com.gplanet.commerce.entities.Usuario;

/**
 * Repository interface for managing Purchase (Compra) entities in the database.
 * Provides CRUD operations and custom queries for purchase-related operations.
 * 
 * @author Gustavo
 * @version 1.0
 */
public interface CompraRepository extends JpaRepository<Compra, Long> {
  
  /**
   * Finds all purchases made by a specific user with pagination support.
   * 
   * @param usuario the user whose purchases are to be retrieved
   * @param pageable pagination information
   * @return a page of purchases made by the user
   */
  Page<Compra> findByUsuario(Usuario usuario, Pageable pageable);

  /**
   * Finds all purchases made between two dates with pagination support.
   * 
   * @param yesterday start date-time (inclusive)
   * @param tomorrow end date-time (inclusive)
   * @param pageable pagination information
   * @return a page of purchases within the specified date range
   */
  Page<Compra> findByFechaBetween(LocalDateTime yesterday, LocalDateTime tomorrow, Pageable pageable);

  /**
   * Finds all purchases made by a user, including their associated products, with pagination support.
   * Uses JOIN FETCH to avoid N+1 query problems.
   * 
   * @param usuarioId the ID of the user
   * @param pageable pagination information
   * @return a page of purchases with their products
   */
  @Query(value = "SELECT c FROM Compra c LEFT JOIN FETCH c.productos WHERE c.usuario.id = :usuarioId",
         countQuery = "SELECT COUNT(c) FROM Compra c WHERE c.usuario.id = :usuarioId")
  Page<Compra> findAllByUsuarioIdWithProductos(@Param("usuarioId") Long usuarioId, Pageable pageable);
}
