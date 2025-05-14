package com.gplanet.commerce.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import com.gplanet.commerce.entities.Usuario;

@DataJpaTest
@ActiveProfiles("test")
class UsuarioRepositoryTest {
  @Autowired
  private UsuarioRepository usuarioRepository;

  @Test
  void save_ValidUser_Success() {
    // Arrange
    Usuario usuario = new Usuario();
    usuario.setNombre("Test User");
    usuario.setEmail("test@example.com");
    usuario.setPassword("password");
    usuario.setRol(Usuario.Role.USER);
    usuario.setFechaCreacion(LocalDateTime.now());

    // Act
    Usuario savedUsuario = usuarioRepository.save(usuario);

    // Assert
    assertNotNull(savedUsuario.getId());
    assertEquals("test@example.com", savedUsuario.getEmail());
    assertEquals(Usuario.Role.USER, savedUsuario.getRol());
  }

  @Test
  void save_DuplicateEmail_ThrowsException() {
    // Arrange
    Usuario usuario1 = new Usuario();
    usuario1.setNombre("User 1");
    usuario1.setEmail("duplicate@example.com");
    usuario1.setPassword("password1");
    usuario1.setRol(Usuario.Role.USER);
    usuarioRepository.save(usuario1);

    Usuario usuario2 = new Usuario();
    usuario2.setNombre("User 2");
    usuario2.setEmail("duplicate@example.com"); // Same email
    usuario2.setPassword("password2");
    usuario2.setRol(Usuario.Role.USER);

    // Act & Assert
    assertThrows(DataIntegrityViolationException.class, () -> {
      usuarioRepository.save(usuario2);
      usuarioRepository.flush();
    });
  }

  @Test
  void findByEmail_ExistingEmail_ReturnsUser() {
    // Arrange
    Usuario usuario = new Usuario();
    usuario.setNombre("Test User");
    usuario.setEmail("test@example.com");
    usuario.setPassword("password");
    usuario.setRol(Usuario.Role.USER);
    usuarioRepository.save(usuario);

    // Act
    Optional<Usuario> foundUsuario = usuarioRepository.findByEmail("test@example.com");

    // Assert
    assertTrue(foundUsuario.isPresent());
    assertEquals("test@example.com", foundUsuario.get().getEmail());
  }

  @Test
  void findByRol_AdminRole_ReturnsAdminUsers() {
    // Arrange
    Usuario adminUser = new Usuario();
    adminUser.setNombre("Admin User");
    adminUser.setEmail("admin@example.com");
    adminUser.setPassword("password");
    adminUser.setRol(Usuario.Role.ADMIN);
    usuarioRepository.save(adminUser);

    Usuario regularUser = new Usuario();
    regularUser.setNombre("Regular User");
    regularUser.setEmail("user@example.com");
    regularUser.setPassword("password");
    regularUser.setRol(Usuario.Role.USER);
    usuarioRepository.save(regularUser);

    // Act
    List<Usuario> adminUsers = usuarioRepository.findByRol(Usuario.Role.ADMIN);

    // Assert
    assertEquals(1, adminUsers.size());
    assertEquals(Usuario.Role.ADMIN, adminUsers.get(0).getRol());
  }
}