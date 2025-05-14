package com.gplanet.commerce.dtos.usuario;

import java.time.LocalDateTime;

import com.gplanet.commerce.entities.Usuario;

/**
 * DTO for returning user information in API responses.
 * This record represents the user data that is sent back to clients,
 * containing non-sensitive user information such as ID, name, email,
 * role, and creation date.
 *
 * @param id            The unique identifier of the user in the database.
 * @param nombre        The full name of the user.
 * @param email         The email address associated with the user account.
 * @param rol           The role assigned to the user in the system.
 * @param fechaCreacion The date and time when the user account was created.
 */
public record UsuarioResponseDTO(
  Long id,
  String nombre,
  String email,
  Usuario.Role rol,
  LocalDateTime fechaCreacion
) {}