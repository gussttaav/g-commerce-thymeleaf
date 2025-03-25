package com.gplanet.commerce.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO for handling password change requests.
 * This class contains the current password and the new password information
 * required to process a password change request.
 *
 * @author Gustavo
 * @version 1.0
 */
@Data
public class CambioPasswdDTO {
    /** 
     * The user's current password for verification. 
     */
    @NotBlank(message = "La contraseña actual es obligatoria")
    private String currentPassword;
    
    /** 
     * The new password to be set for the user account. 
     */
    @NotBlank(message = "La nueva contraseña es obligatoria")
    private String newPassword;
    
    /** 
     * Confirmation of the new password to prevent typing errors. 
     */
    @NotBlank(message = "La confirmación de contraseña es obligatoria")
    private String confirmPassword;
}
