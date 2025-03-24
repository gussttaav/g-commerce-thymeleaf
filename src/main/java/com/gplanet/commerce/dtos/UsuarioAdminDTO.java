package com.gplanet.commerce.dtos;

import com.gplanet.commerce.entities.Usuario;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO for administrative user operations.
 * Extends the basic user DTO and adds role information for administrative purposes.
 * This class is used when administrators create or modify user accounts with specific roles.
 * 
 * @author Gustavo
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UsuarioAdminDTO extends UsuarioDTO {

    /**
     * The role assigned to the user in the system. Defaults to USER role.
     */
    private Usuario.Role rol = Usuario.Role.USER;
}
