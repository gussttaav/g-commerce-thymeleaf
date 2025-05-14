package com.gplanet.commerce.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gplanet.commerce.entities.Producto;

/**
 * Repository interface for managing Product (Producto) entities in the database.
 * Provides CRUD operations and custom queries for product-related operations.
 * 
 * @author Gustavo
 * @version 1.0
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
  
  /**
   * Finds all active products in the system with pagination support.
   * 
   * @param pageable pagination information
   * @return a page of products where the 'activo' flag is true
   */
  Page<Producto> findByActivoTrue(Pageable pageable);

  /**
   * Finds all non-active products in the system with pagination support.
   * 
   * @param pageable pagination information
   * @return a page of products where the 'activo' flag is false
   */
  Page<Producto> findByActivoFalse(Pageable pageable);

  /**
   * Finds active products containing the search term in either name or description.
   * 
   * @param searchTerm The search term to match against name or description (should include % wildcards)
   * @param pageable Pagination information
   * @return Page of Producto entities matching the criteria
   */
  @Query("SELECT p FROM Producto p "+
         "WHERE p.activo = true AND (LOWER(p.nombre) LIKE :searchTerm OR LOWER(p.descripcion) LIKE :searchTerm)")
  Page<Producto> findByActivoTrueAndSearch(String searchTerm, Pageable pageable);

  /**
   * Finds inactive products containing the search term in either name or description.
   * 
   * @param searchTerm The search term to match against name or description (should include % wildcards)
   * @param pageable Pagination information
   * @return Page of Producto entities matching the criteria
   */
  @Query("SELECT p FROM Producto p " + 
         "WHERE p.activo = false AND (LOWER(p.nombre) LIKE :searchTerm OR LOWER(p.descripcion) LIKE :searchTerm)")
  Page<Producto> findByActivoFalseAndSearch(String searchTerm, Pageable pageable);

  /**
   * Finds all products (active and inactive) containing the search term in either name or description.
   * 
   * @param searchTerm The search term to match against name or description (should include % wildcards)
   * @param pageable Pagination information
   * @return Page of Producto entities matching the criteria
   */
  @Query("SELECT p FROM Producto p "+
         "WHERE LOWER(p.nombre) LIKE :searchTerm OR LOWER(p.descripcion) LIKE :searchTerm")
  Page<Producto> findBySearch(String searchTerm, Pageable pageable);
}
