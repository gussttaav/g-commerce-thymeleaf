package com.gplanet.commerce.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gplanet.commerce.dtos.ActualizacionUsuarioDTO;
import com.gplanet.commerce.dtos.CambioPasswdDTO;
import com.gplanet.commerce.dtos.UsuarioAdminDTO;
import com.gplanet.commerce.dtos.UsuarioDTO;
import com.gplanet.commerce.entities.Usuario;
import com.gplanet.commerce.exceptions.EmailAlreadyExistsException;
import com.gplanet.commerce.exceptions.InvalidPasswordException;
import com.gplanet.commerce.exceptions.PasswordMismatchException;
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

    public Usuario buscarPorEmail(String email){
        return usuarioRepository.findByEmail(email).orElseThrow(
            () -> new UsernameNotFoundException("User not found with email: " + email)
        );
    }

    /**
     * Updates a user's profile information.
     * 
     * @param email Current email of the user
     * @param perfilDTO Data transfer object containing new profile information
     * @throws UsernameNotFoundException if user is not found
     */
    @Transactional
    public void actualizarPerfil(String email, ActualizacionUsuarioDTO perfilDTO) {
        log.info("Updating profile for user: {}", email);
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        String nuevoEmail = perfilDTO.getNuevoEmail();
        if(!email.equals(perfilDTO.getNuevoEmail()) && usuarioRepository.existsByEmail(nuevoEmail)){
            log.warn("Updating profile failed, email already exists: {}", nuevoEmail);
            throw new EmailAlreadyExistsException(nuevoEmail);
        }

        usuario.setEmail(perfilDTO.getNuevoEmail());
        usuario.setNombre(perfilDTO.getNombre());
        
        usuarioRepository.save(usuario);
        log.info("Profile updated successfully for user: {}", usuario.getEmail());
    }

        /**
     * Changes a user's password after validating current password.
     * 
     * @param email Email of the user
     * @param contraseñaDTO Data transfer object containing password change information
     * @throws UsernameNotFoundException if user is not found
     * @throws InvalidPasswordException if current password is incorrect or new password is same as current
     * @throws PasswordMismatchException if new password and confirmation don't match
     */
    @Transactional
    public void cambiarContraseña(String email, CambioPasswdDTO contraseñaDTO) {
        log.info("Password change attempt for user: {}", email);
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // Verify current password
        if (!passwordEncoder.matches(contraseñaDTO.getCurrentPassword(), usuario.getPassword())) {
            throw new InvalidPasswordException("The current password is incorrect");
        }

        // Verify new password is not the same as current
        if (passwordEncoder.matches(contraseñaDTO.getNewPassword(), usuario.getPassword())) {
            throw new InvalidPasswordException("The new password cannot be the same as the current password");
        }

        // Verify password confirmation
        if (!contraseñaDTO.getNewPassword().equals(contraseñaDTO.getConfirmPassword())) {
            throw new PasswordMismatchException("The new password and confirmation do not match");
        }

        // Update password
        usuario.setPassword(passwordEncoder.encode(contraseñaDTO.getNewPassword()));
        usuarioRepository.save(usuario);
        log.info("Password successfully changed for user: {}", email);
    }
}
