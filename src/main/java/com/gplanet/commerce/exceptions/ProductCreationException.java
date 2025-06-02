package com.gplanet.commerce.exceptions;

/**
 * Exception thrown when there is an error during product creation.
 * This exception is typically thrown by the product service layer when 
 * product creation fails due to business rule violations or other errors.
 * 
 * @author Gustavo
 * @version 1.0
 */
public class ProductCreationException extends RuntimeException {

  /**
   * Creates a new product creation exception.
   * 
   * @param message The detailed message explaining the creation error
   */
  public ProductCreationException(String message) {
    super(message);
  }
}