package com.gplanet.commerce.exceptions;

/**
 * Exception thrown when a requested resource (such as a user, product, or purchase)
 * cannot be found in the system.
 * 
 * This exception is typically thrown when attempting to access, modify, or delete
 * a resource that doesn't exist in the database.
 * 
 * @author Gustavo
 * @version 1.0
 */
public class ResourceNotFoundException extends RuntimeException {
    /**
     * Creates a new resource not found exception.
     * 
     * @param message The detailed message explaining which resource was not found
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
