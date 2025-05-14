package com.gplanet.commerce.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

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

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

  @Mock
  private UsuarioRepository usuarioRepository;

  @Mock
  private UsuarioMapper usuarioMapper;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UsuarioService usuarioService;

  private Usuario usuario;
  private UsuarioDTO usuarioDTO;
  private UsuarioAdminDTO usuarioAdminDTO;
  private UsuarioResponseDTO usuarioResponseDTO;
  private ActualizacionUsuarioDTO actualizacionUsuarioDTO;
  private CambioPasswdDTO cambioPasswdDTO;

  @BeforeEach
  void setUp() {
    // Set up test data
    usuario = new Usuario();
    usuario.setId(1L);
    usuario.setNombre("Test User");
    usuario.setEmail("test@example.com");
    usuario.setPassword("encodedPassword");
    usuario.setRol(Usuario.Role.USER);
    usuario.setFechaCreacion(LocalDateTime.now());

    usuarioDTO = new UsuarioDTO();
    usuarioDTO.setNombre("Test User");
    usuarioDTO.setEmail("test@example.com");
    usuarioDTO.setPassword("password123");

    usuarioAdminDTO = new UsuarioAdminDTO();
    usuarioAdminDTO.setNombre("Admin User");
    usuarioAdminDTO.setEmail("admin@example.com");
    usuarioAdminDTO.setPassword("adminPass123");
    usuarioAdminDTO.setRol(Usuario.Role.ADMIN);

    usuarioResponseDTO = new UsuarioResponseDTO(
        1L,
        "Test User",
        "test@example.com",
        Usuario.Role.USER,
        LocalDateTime.now());

    actualizacionUsuarioDTO = new ActualizacionUsuarioDTO(
        "Updated Name",
        "updated@example.com");

    cambioPasswdDTO = new CambioPasswdDTO();
    cambioPasswdDTO.setCurrentPassword("password123");
    cambioPasswdDTO.setNewPassword("newPassword123");
    cambioPasswdDTO.setConfirmPassword("newPassword123");
  }

  @Test
  void registrarUsuario_ConDatosValidos_RetornaUsuarioCreado() {
    // Arrange
    when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
    when(usuarioMapper.toUsuarioResponseDTO(any(Usuario.class))).thenReturn(usuarioResponseDTO);

    // Act
    UsuarioResponseDTO result = usuarioService.registrarUsuario(usuarioDTO);

    // Assert
    assertNotNull(result);
    assertEquals(usuarioResponseDTO, result);
    verify(usuarioRepository).existsByEmail(usuarioDTO.getEmail());
    verify(passwordEncoder).encode(usuarioDTO.getPassword());
    verify(usuarioRepository).save(any(Usuario.class));
    verify(usuarioMapper).toUsuarioResponseDTO(any(Usuario.class));
  }

  @Test
  void registrarUsuario_ConEmailExistente_LanzaExcepcion() {
    // Arrange
    when(usuarioRepository.existsByEmail(anyString())).thenReturn(true);

    // Act & Assert
    assertThrows(EmailAlreadyExistsException.class, () -> {
      usuarioService.registrarUsuario(usuarioDTO);
    });
    verify(usuarioRepository).existsByEmail(usuarioDTO.getEmail());
    verify(usuarioRepository, never()).save(any(Usuario.class));
  }

  @Test
  void registrarUsuarioAdmin_ConDatosValidos_AsignaRolAdmin() {
    // Arrange
    when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
    when(usuarioMapper.toUsuarioResponseDTO(any(Usuario.class))).thenReturn(usuarioResponseDTO);

    // Act
    usuarioService.registrarUsuario(usuarioAdminDTO);

    // Assert
    verify(usuarioRepository).save(argThat(savedUser -> savedUser.getRol() == Usuario.Role.ADMIN));
  }

  @Test
  void buscarPorEmail_ConEmailExistente_RetornaUsuario() {
    // Arrange
    when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(usuario));

    // Act
    Usuario result = usuarioService.buscarPorEmail("test@example.com");

    // Assert
    assertNotNull(result);
    assertEquals(usuario, result);
    verify(usuarioRepository).findByEmail("test@example.com");
  }

  @Test
  void buscarPorEmail_ConEmailInexistente_LanzaExcepcion() {
    // Arrange
    when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(UsernameNotFoundException.class, () -> {
      usuarioService.buscarPorEmail("nonexistent@example.com");
    });
    verify(usuarioRepository).findByEmail("nonexistent@example.com");
  }

  @Test
  void actualizarPerfil_ConDatosValidos_ActualizaUsuario() {
    // Arrange
    when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(usuario));
    when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);

    // Act
    usuarioService.actualizarPerfil("test@example.com", actualizacionUsuarioDTO);

    // Assert
    verify(usuarioRepository).findByEmail("test@example.com");
    verify(usuarioRepository).existsByEmail("updated@example.com");
    verify(usuarioRepository).save(argThat(savedUser -> savedUser.getNombre().equals("Updated Name") &&
        savedUser.getEmail().equals("updated@example.com")));
  }

  @Test
  void actualizarPerfil_ConEmailExistente_LanzaExcepcion() {
    // Arrange
    when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(usuario));
    when(usuarioRepository.existsByEmail("updated@example.com")).thenReturn(true);

    // Act & Assert
    assertThrows(EmailAlreadyExistsException.class, () -> {
      usuarioService.actualizarPerfil("test@example.com", actualizacionUsuarioDTO);
    });
    verify(usuarioRepository).findByEmail("test@example.com");
    verify(usuarioRepository).existsByEmail("updated@example.com");
    verify(usuarioRepository, never()).save(any(Usuario.class));
  }

  @Test
  void actualizarPerfil_ConUsuarioInexistente_LanzaExcepcion() {
    // Arrange
    when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(UsernameNotFoundException.class, () -> {
      usuarioService.actualizarPerfil("nonexistent@example.com", actualizacionUsuarioDTO);
    });
    verify(usuarioRepository).findByEmail("nonexistent@example.com");
    verify(usuarioRepository, never()).save(any(Usuario.class));
  }

  @Test
  void cambiarContraseña_ConDatosValidos_ActualizaContraseña() {
    // Arrange
    when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(usuario));
    when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
    when(passwordEncoder.matches("newPassword123", "encodedPassword")).thenReturn(false);
    when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");

    // Act
    usuarioService.changePassword("test@example.com", cambioPasswdDTO);

    // Assert
    verify(usuarioRepository).findByEmail("test@example.com");
    verify(passwordEncoder).matches("password123", "encodedPassword");
    verify(passwordEncoder).matches("newPassword123", "encodedPassword");
    verify(passwordEncoder).encode("newPassword123");
    verify(usuarioRepository).save(argThat(savedUser -> savedUser.getPassword().equals("newEncodedPassword")));
  }

  @Test
  void cambiarContraseña_ConContraseñaActualIncorrecta_LanzaExcepcion() {
    // Arrange
    when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(usuario));
    when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

    // Act & Assert
    assertThrows(InvalidPasswordException.class, () -> {
      usuarioService.changePassword("test@example.com", cambioPasswdDTO);
    });
    verify(usuarioRepository).findByEmail("test@example.com");
    verify(passwordEncoder).matches("password123", "encodedPassword");
    verify(usuarioRepository, never()).save(any(Usuario.class));
  }

  @Test
  void cambiarContraseña_ConNuevaContraseñaIgualActual_LanzaExcepcion() {
    // Arrange
    when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(usuario));
    when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
    when(passwordEncoder.matches("newPassword123", "encodedPassword")).thenReturn(true);

    // Act & Assert
    assertThrows(InvalidPasswordException.class, () -> {
      usuarioService.changePassword("test@example.com", cambioPasswdDTO);
    });
    verify(usuarioRepository).findByEmail("test@example.com");
    verify(passwordEncoder).matches("password123", "encodedPassword");
    verify(passwordEncoder).matches("newPassword123", "encodedPassword");
    verify(usuarioRepository, never()).save(any(Usuario.class));
  }

  @Test
  void cambiarContraseña_ConConfirmacionNoCoincide_LanzaExcepcion() {
    // Arrange
    when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(usuario));
    when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
    when(passwordEncoder.matches("newPassword123", "encodedPassword")).thenReturn(false);

    cambioPasswdDTO.setConfirmPassword("differentPassword");

    // Act & Assert
    assertThrows(PasswordMismatchException.class, () -> {
      usuarioService.changePassword("test@example.com", cambioPasswdDTO);
    });
    verify(usuarioRepository).findByEmail("test@example.com");
    verify(passwordEncoder).matches("password123", "encodedPassword");
    verify(passwordEncoder).matches("newPassword123", "encodedPassword");
    verify(usuarioRepository, never()).save(any(Usuario.class));
  }

  @Test
  void listarUsuarios_RetornaPaginaDeUsuarios() {
    // Arrange
    List<Usuario> usuariosList = List.of(usuario);
    Page<Usuario> usuariosPage = new PageImpl<>(usuariosList);
    Page<UsuarioResponseDTO> expectedResponsePage = new PageImpl<>(List.of(usuarioResponseDTO));

    when(usuarioRepository.findAll(any(Pageable.class))).thenReturn(usuariosPage);
    when(usuarioMapper.toUsuarioResponseDTO(usuario)).thenReturn(usuarioResponseDTO);

    // Act
    Page<UsuarioResponseDTO> resultPage = usuarioService.listarUsuarios(0, 10, "nombre", "ASC");

    // Assert
    assertNotNull(resultPage);
    assertEquals(1, resultPage.getTotalElements());
    assertEquals(expectedResponsePage.getContent().get(0), resultPage.getContent().get(0));
    verify(usuarioRepository).findAll(any(Pageable.class));
    verify(usuarioMapper).toUsuarioResponseDTO(usuario);
  }

  @Test
  void cambiarRol_DeUserAAdmin_CambiaRolYRetornaUsuario() {
    // Arrange
    Usuario usuarioUser = new Usuario();
    usuarioUser.setId(1L);
    usuarioUser.setRol(Usuario.Role.USER);

    Usuario usuarioAdmin = new Usuario();
    usuarioAdmin.setId(1L);
    usuarioAdmin.setRol(Usuario.Role.ADMIN);

    UsuarioResponseDTO adminResponseDTO = new UsuarioResponseDTO(
        1L, "Test User", "test@example.com", Usuario.Role.ADMIN, LocalDateTime.now());

    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioUser));
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioAdmin);
    when(usuarioMapper.toUsuarioResponseDTO(any(Usuario.class))).thenReturn(adminResponseDTO);

    // Act
    UsuarioResponseDTO result = usuarioService.cambiarRol(1L);

    // Assert
    assertNotNull(result);
    assertEquals(Usuario.Role.ADMIN, result.rol());
    verify(usuarioRepository).findById(1L);
    verify(usuarioRepository).save(argThat(savedUser -> savedUser.getRol() == Usuario.Role.ADMIN));
    verify(usuarioMapper).toUsuarioResponseDTO(any(Usuario.class));
  }

  @Test
  void cambiarRol_DeAdminAUser_CambiaRolYRetornaUsuario() {
    // Arrange
    Usuario usuarioAdmin = new Usuario();
    usuarioAdmin.setId(1L);
    usuarioAdmin.setRol(Usuario.Role.ADMIN);

    Usuario usuarioUser = new Usuario();
    usuarioUser.setId(1L);
    usuarioUser.setRol(Usuario.Role.USER);

    UsuarioResponseDTO userResponseDTO = new UsuarioResponseDTO(
        1L, "Test User", "test@example.com", Usuario.Role.USER, LocalDateTime.now());

    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioAdmin));
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioUser);
    when(usuarioMapper.toUsuarioResponseDTO(any(Usuario.class))).thenReturn(userResponseDTO);

    // Act
    UsuarioResponseDTO result = usuarioService.cambiarRol(1L);

    // Assert
    assertNotNull(result);
    assertEquals(Usuario.Role.USER, result.rol());
    verify(usuarioRepository).findById(1L);
    verify(usuarioRepository).save(argThat(savedUser -> savedUser.getRol() == Usuario.Role.USER));
    verify(usuarioMapper).toUsuarioResponseDTO(any(Usuario.class));
  }

  @Test
  void cambiarRol_ConUsuarioInexistente_LanzaExcepcion() {
    // Arrange
    when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(ResourceNotFoundException.class, () -> {
      usuarioService.cambiarRol(99L);
    });
    verify(usuarioRepository).findById(99L);
    verify(usuarioRepository, never()).save(any(Usuario.class));
  }

  @Test
  void obtenerPerfil_ConUsuarioExistente_RetornaUsuario() {
    // Arrange
    when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(usuario));
    when(usuarioMapper.toUsuarioResponseDTO(any(Usuario.class))).thenReturn(usuarioResponseDTO);

    // Act
    UsuarioResponseDTO result = usuarioService.obtenerPerfil("test@example.com");

    // Assert
    assertNotNull(result);
    assertEquals(usuarioResponseDTO, result);
    verify(usuarioRepository).findByEmail("test@example.com");
    verify(usuarioMapper).toUsuarioResponseDTO(usuario);
  }

  @Test
  void obtenerPerfil_ConUsuarioInexistente_LanzaExcepcion() {
    // Arrange
    when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(UsernameNotFoundException.class, () -> {
      usuarioService.obtenerPerfil("nonexistent@example.com");
    });
    verify(usuarioRepository).findByEmail("nonexistent@example.com");
    verify(usuarioMapper, never()).toUsuarioResponseDTO(any(Usuario.class));
  }
}