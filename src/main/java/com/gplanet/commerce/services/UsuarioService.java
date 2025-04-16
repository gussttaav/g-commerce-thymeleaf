package com.gplanet.commerce.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gplanet.commerce.dtos.usuario.ActualizacionUsuarioDTO;
import com.gplanet.commerce.dtos.usuario.CambioPasswdDTO;
import com.gplanet.commerce.dtos.usuario.UsuarioAdminDTO;
import com.gplanet.commerce.dtos.usuario.UsuarioDTO;
import com.gplanet.commerce.dtos.usuario.UsuarioMapper;
import com.gplanet.commerce.dtos.usuario.UsuarioResponseDTO;
import com.gplanet.commerce.entities.Usuario;
import com.gplanet.commerce.exceptions.EmailAlreadyExistsException;
import com.gplanet.commerce.exceptions.InvalidPasswordException;
import com.gplanet.commerce.exceptions.PasswordMismatchException;
import com.gplanet.commerce.exceptions.ResourceNotFoundException;
import com.gplanet.commerce.repositories.UsuarioRepository;

import java.time.LocalDateTime;

/**
 * Service class that handles user-related operations including registration,
 * authentication, profile updates, and password management.
 * 
 * This class provides comprehensive user management functionality including
 * user registration, profile updates, password changes, and role management.
 * It also supports pagination for user listing operations.
 * 
 * @author Gustavo
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {
    
    private final UsuarioMapper usuarioMapper;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new user in the system.
     * 
     * @param usuarioDTO Data transfer object containing user registration information
     */
    @Transactional
    public UsuarioResponseDTO registrarUsuario(UsuarioDTO usuarioDTO){
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
            usuario.setRol(((UsuarioAdminDTO) usuarioDTO).getRol());
        }else{
            usuario.setRol(Usuario.Role.USER);
        }
        usuario.setFechaCreacion(LocalDateTime.now());
            
        Usuario savedUsuario = usuarioRepository.save(usuario);
        log.info("User registered successfully: {}", usuarioDTO.getEmail());

        return usuarioMapper.toUsuarioResponseDTO(savedUsuario);
    }

    /**
     * Finds a user by their email address.
     * 
     * @param email The email address to search for
     * @return Usuario The found user entity
     * @throws UsernameNotFoundException if no user is found with the given email
     */
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

    /**
     * Lists all users in the system with pagination support.
     * 
     * @param page The page number (zero-based)
     * @param size The page size
     * @param sort The field to sort by
     * @param direction The sort direction (ASC or DESC)
     * @return Page of UsuarioResponseDTO containing paginated users' information
     */
    public Page<UsuarioResponseDTO> listarUsuarios(int page, int size, String sort, String direction) {
        log.debug("Listing users with pagination - page: {}, size: {}, sort: {}, direction: {}", page, size, sort, direction);
        
        // Create Pageable object with sort direction
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        // Get paginated result
        Page<Usuario> usuariosPage = usuarioRepository.findAll(pageable);
        
        // Map to DTOs
        Page<UsuarioResponseDTO> result = usuariosPage.map(usuarioMapper::toUsuarioResponseDTO);
        
        log.debug("Found {} users on page {} of {}", 
                result.getNumberOfElements(), 
                result.getNumber() + 1,  // +1 for human-readable page number
                result.getTotalPages());
                
        return result;
    }

    /**
     * Changes the role of a user between ADMIN and USER.
     * 
     * @param userId The ID of the user whose role should be changed
     * @return UsuarioResponseDTO containing the updated user information
     * @throws ResourceNotFoundException if the user is not found
     */
    @Transactional
    public UsuarioResponseDTO cambiarRol(Long userId) {
        log.info("Changing rolefor user ID: {}", userId);
        Usuario usuario = usuarioRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No existe ningún usuario con el ID proporcionado"
            ));
        
        usuario.setRol(
            usuario.getRol().equals(Usuario.Role.ADMIN) 
            ? Usuario.Role.USER : Usuario.Role.ADMIN
        );

        Usuario savedUser = usuarioRepository.save(usuario);
        log.info("Role successfully updated for user ID: {}", userId);

        return usuarioMapper.toUsuarioResponseDTO(savedUser);
    }

    /**
     * Retrieves a user's profile information.
     * 
     * @param email Email of the user
     * @return UsuarioResponseDTO containing the user's information
     * @throws UsernameNotFoundException if user is not found
     */
    public UsuarioResponseDTO obtenerPerfil(String email) {
        log.debug("Retrieving profile for user: {}", email);
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return usuarioMapper.toUsuarioResponseDTO(usuario);
    }
}
