package com.gplanet.commerce.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.gplanet.commerce.entities.Usuario;

/**
 * Custom UserDetails implementation that extends Spring Security's User class.
 * Adds additional user information like name for use in the application.
 * 
 * @author Gustavo
 * @version 1.0
 */
public class UsuarioDetalles extends User {

    private final String nombre;

    /**
     * Creates a new UserDetails instance with user information and authorities.
     * 
     * @param usuario The user entity containing the user's information
     * @param authorities Collection of granted authorities for the user
     */
    public UsuarioDetalles(Usuario usuario, Collection<? extends GrantedAuthority> authorities) {
        super(usuario.getEmail(), usuario.getPassword(), authorities);
        this.nombre = usuario.getNombre();
    }
    
    /**
     * Gets the user's full name.
     * 
     * @return The user's full name
     */
    public String getNombre() {
        return nombre;
    }
    
}
