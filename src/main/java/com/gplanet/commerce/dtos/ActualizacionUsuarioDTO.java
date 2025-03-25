package com.gplanet.commerce.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO for user profile update operations.
 * This class encapsulates the user information that can be updated,
 * including the user's name and email address.
 *
 * @author Gustavo
 * @version 1.0
 */
@Data
@AllArgsConstructor
public class ActualizacionUsuarioDTO {
    /** 
     * The updated full name of the user. 
     */
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    
    /** 
     * The new email address for the user account. 
     */
    @Email(message = "El email debe ser válido")
    @NotBlank(message = "El email es obligatorio")
    private String nuevoEmail;
}
