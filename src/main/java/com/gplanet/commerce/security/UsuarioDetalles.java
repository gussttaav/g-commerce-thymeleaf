package com.gplanet.commerce.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.gplanet.commerce.entities.Usuario;

public class UsuarioDetalles extends User {

    private final String nombre;

    public UsuarioDetalles(Usuario usuario, Collection<? extends GrantedAuthority> authorities) {
        super(usuario.getEmail(), usuario.getPassword(), authorities);
        this.nombre = usuario.getNombre();
    }
    
    public String getNombre() {
        return nombre;
    }
    
}
