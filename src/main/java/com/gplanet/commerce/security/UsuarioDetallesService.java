package com.gplanet.commerce.security;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.gplanet.commerce.entities.Usuario;
import com.gplanet.commerce.repositories.UsuarioRepository;

import java.util.Collections;

/**
 * Service class that implements Spring Security's UserDetailsService.
 * Provides user authentication and authority information to Spring Security.
 * 
 * @author Gustavo
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class UsuarioDetallesService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Loads user details by email for Spring Security authentication.
     * 
     * @param email The email of the user to load
     * @return UsuarioDetalles object containing user's security information
     * @throws UsernameNotFoundException if user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return new UsuarioDetalles(usuario,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + usuario.getRol()))
        );
    }
}