package com.gplanet.commerce.exceptions;

/**
 * Exception thrown when attempting to register or update a user with an email
 * that already exists in the system.
 * 
 * @author Gustavo
 * @version 1.0
 */
public class EmailAlreadyExistsException extends RuntimeException {
    /**
     * Creates a new email already exists exception.
     * 
     * @param email The email address that caused the conflict
     */
    public EmailAlreadyExistsException(String email) {
        super("A user with email '" + email + "' already exists");
    }
}