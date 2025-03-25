package com.gplanet.commerce.exceptions;

/**
 * Exception thrown when password confirmation doesn't match the original password.
 * Typically used during password change or user registration operations.
 * 
 * @author Gustavo
 * @version 1.0
 */
public class PasswordMismatchException extends RuntimeException {
    /**
     * Creates a new password mismatch exception.
     * 
     * @param message The detailed message explaining the mismatch
     */
    public PasswordMismatchException(String message) {
        super(message);
    }
}