package com.gplanet.commerce.exceptions;

/**
 * Exception thrown when a password validation fails.
 * This could be due to incorrect current password, weak password format,
 * or when the new password matches the current one during a password change.
 * 
 * @author Gustavo
 * @version 1.0
 */
public class InvalidPasswordException extends RuntimeException {
  /**
   * Creates a new invalid password exception.
   * 
   * @param message The detailed message explaining why the password is invalid
   */
  public InvalidPasswordException(String message) {
    super(message);
  }
}