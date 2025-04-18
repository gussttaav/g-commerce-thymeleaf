package com.gplanet.commerce.dtos.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for user profile update operations.
 * This record encapsulates the user information that can be updated,
 * including the user's name and email address.
 *
 * @param nombre     The updated full name of the user.
 * @param nuevoEmail The new email address for the user account.
 */
public record ActualizacionUsuarioDTO(
    @NotBlank(message = "El nombre es obligatorio")
    String nombre,

    @Email(message = "El email debe ser válido")
    @NotBlank(message = "El email es obligatorio")
    String nuevoEmail
) {}