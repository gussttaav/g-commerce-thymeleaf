package com.gplanet.commerce.dtos.usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

import com.gplanet.commerce.entities.Usuario;

/**
 * DTO for returning user information in API responses.
 * This class represents the user data that is sent back to clients,
 * containing non-sensitive user information such as ID, name, email,
 * role, and creation date.
 *
 * @author Gustavo
 * @version 1.0
 */
@Data
@AllArgsConstructor
@Builder
public class UsuarioResponseDTO {
    /** 
     * The unique identifier of the user in the database. 
     * */
    private Long id;
    
    /** 
     * The full name of the user. 
     */
    private String nombre;
    
    /** 
     * The email address associated with the user account. 
     */
    private String email;
    
    /** 
     * The role assigned to the user in the system. 
     */
    private Usuario.Role rol;
    
    /** 
     * The date and time when the user account was created. 
     */
    private LocalDateTime fechaCreacion;
}
