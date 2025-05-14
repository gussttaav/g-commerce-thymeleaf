package com.gplanet.commerce.dtos.producto;

/**
 * Represents the different status filters available for products in the system.
 * This enum is used to filter products in the service layer operations.
 * Each status value corresponds to a specific filtering criteria for retrieving products.
 * 
 * @author Gustavo
 * @version 1.0
 */
public enum ProductStatus {
  /**
   * Represents products that are currently active in the system.
   * Active products are those with their 'activo' flag set to true.
   */
  ACTIVE,

  /**
   * Represents products that are currently inactive in the system.
   * Inactive products are those with their 'activo' flag set to false.
   */
  INACTIVE,

  /**
   * Represents all products in the system regardless of their active status.
   * This option retrieves both active and inactive products.
   */
  ALL
}