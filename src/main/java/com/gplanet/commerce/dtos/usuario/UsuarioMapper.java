package com.gplanet.commerce.dtos.usuario;

import org.mapstruct.Mapper;

import com.gplanet.commerce.entities.Usuario;

/**
 * Mapper interface for converting between Usuario entity and DTOs.
 * This interface uses MapStruct to generate the implementation for
 * converting Usuario entities to UsuarioResponseDTO objects.
 *
 * @author Gustavo
 * @version 1.0
 */
@Mapper(componentModel = "spring")
public interface UsuarioMapper {
  /**
   * Converts a Usuario entity to a UsuarioResponseDTO.
   *
   * @param usuario the Usuario entity to convert
   * @return the corresponding UsuarioResponseDTO
   */
  UsuarioResponseDTO toUsuarioResponseDTO(Usuario usuario);
}
