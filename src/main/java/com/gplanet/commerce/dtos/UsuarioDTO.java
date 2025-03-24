package com.gplanet.commerce.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Base DTO for user registration operations.
 * This class contains the basic user information required for registration,
 * including name, email, and password.
 *
 * @author Gustavo
 * @version 1.0
 */
@Data
public class UsuarioDTO {
    /** 
     * The full name of the user. 
     */
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    
    /** 
     * The email address for the user account, used as username. 
     */
    @Email(message = "El email debe ser válido")
    @NotBlank(message = "El email es obligatorio")
    private String email;
    
    /** 
     * The password for the user account. 
     */
    @NotBlank(message = "La contraseña es obligatoria")
    private String password; 
}
