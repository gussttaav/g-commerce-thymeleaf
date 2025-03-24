package com.gplanet.commerce.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gplanet.commerce.dtos.UsuarioAdminDTO;
import com.gplanet.commerce.dtos.UsuarioDTO;
import com.gplanet.commerce.entities.Usuario;
import com.gplanet.commerce.exceptions.EmailAlreadyExistsException;
import com.gplanet.commerce.repositories.UsuarioRepository;

import java.time.LocalDateTime;

/**
 * Service class that handles user-related operations including registration,
 * authentication, profile updates, and password management.
 * 
 * @author Gustavo
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {
    
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new user in the system.
     * 
     * @param usuarioDTO Data transfer object containing user registration information
     */
    @Transactional
    public void registrarUsuario(UsuarioDTO usuarioDTO){
        log.info("Registering new user with email: {}", usuarioDTO.getEmail());

        if(usuarioRepository.existsByEmail(usuarioDTO.getEmail())){
            log.warn("Registration failed: email already exists: {}", usuarioDTO.getEmail());
            throw new EmailAlreadyExistsException(usuarioDTO.getEmail());
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));

        if(usuarioDTO instanceof UsuarioAdminDTO){
            usuario.setRol(Usuario.Role.ADMIN);
        }else{
            usuario.setRol(Usuario.Role.USER);
        }
        usuario.setFechaCreacion(LocalDateTime.now());
            
        usuarioRepository.save(usuario);
        log.info("User registered successfully: {}", usuarioDTO.getEmail());
    }
}
