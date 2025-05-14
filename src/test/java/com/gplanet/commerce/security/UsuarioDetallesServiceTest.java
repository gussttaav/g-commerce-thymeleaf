package com.gplanet.commerce.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.gplanet.commerce.entities.Usuario;
import com.gplanet.commerce.repositories.UsuarioRepository;

public class UsuarioDetallesServiceTest {

  private UsuarioRepository usuarioRepository;
  private UsuarioDetallesService usuarioDetallesService;

  @BeforeEach
  public void setUp() {
    usuarioRepository = mock(UsuarioRepository.class);
    usuarioDetallesService = new UsuarioDetallesService(usuarioRepository);
  }

  @Test
  public void loadUserByUsername_UserExists_ReturnsUserDetails() {
    // Arrange
    Usuario usuario = new Usuario();
    usuario.setEmail("test@example.com");
    usuario.setPassword("password");
    usuario.setNombre("Test User");
    usuario.setRol(Usuario.Role.USER);

    when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));

    // Act
    UserDetails userDetails = usuarioDetallesService.loadUserByUsername("test@example.com");

    // Assert
    assertEquals("test@example.com", userDetails.getUsername());
    assertEquals("password", userDetails.getPassword());
    assertTrue(userDetails.getAuthorities().stream()
        .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    assertTrue(userDetails instanceof UsuarioDetalles);
    assertEquals("Test User", ((UsuarioDetalles) userDetails).getNombre());
  }

  @Test
  public void loadUserByUsername_UserDoesNotExist_ThrowsException() {
    // Arrange
    when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(UsernameNotFoundException.class, () -> {
      usuarioDetallesService.loadUserByUsername("nonexistent@example.com");
    });
  }

  @Test
  public void loadUserByUsername_AdminUser_HasAdminRole() {
    // Arrange
    Usuario admin = new Usuario();
    admin.setEmail("admin@example.com");
    admin.setPassword("adminpass");
    admin.setNombre("Admin User");
    admin.setRol(Usuario.Role.ADMIN);

    when(usuarioRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));

    // Act
    UserDetails userDetails = usuarioDetallesService.loadUserByUsername("admin@example.com");
    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

    // Assert
    assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    assertEquals(1, authorities.size());
  }
}