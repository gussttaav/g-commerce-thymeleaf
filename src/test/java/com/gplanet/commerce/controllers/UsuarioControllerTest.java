package com.gplanet.commerce.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.gplanet.commerce.dtos.usuario.ActualizacionUsuarioDTO;
import com.gplanet.commerce.dtos.usuario.CambioPasswdDTO;
import com.gplanet.commerce.dtos.usuario.UsuarioAdminDTO;
import com.gplanet.commerce.dtos.usuario.UsuarioDTO;
import com.gplanet.commerce.dtos.usuario.UsuarioResponseDTO;
import com.gplanet.commerce.entities.Usuario;
import com.gplanet.commerce.exceptions.EmailAlreadyExistsException;
import com.gplanet.commerce.exceptions.InvalidPasswordException;
import com.gplanet.commerce.exceptions.PasswordMismatchException;
import com.gplanet.commerce.exceptions.ResourceNotFoundException;
import com.gplanet.commerce.security.SecurityConfig;
import com.gplanet.commerce.security.UsuarioDetalles;
import com.gplanet.commerce.security.UsuarioDetallesService;
import com.gplanet.commerce.services.UsuarioService;

@WebMvcTest(UsuarioController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UsuarioControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext context;

  @MockitoBean
  private UsuarioService usuarioService;

  @MockitoBean
  private UsuarioDetallesService usuarioDetallesService;

  private UsuarioDTO usuarioDTO;
  private CambioPasswdDTO cambioPasswdDTO;
  private UsuarioResponseDTO usuarioResponseDTO;
  private UsuarioAdminDTO usuarioAdminDTO;

  private Usuario testUser;
  private UsuarioDetalles userDetails;
  private Usuario adminUser;
  private UsuarioDetalles adminDetails;

  @BeforeEach
  void setUp() {

    mockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(springSecurity())
        .build();

    // Set up a test user
    testUser = new Usuario();
    testUser.setEmail("test@example.com");
    testUser.setPassword("$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG"); // "password" encoded
    testUser.setNombre("Test User");
    testUser.setRol(Usuario.Role.USER);
    userDetails = new UsuarioDetalles(testUser, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    when(usuarioDetallesService.loadUserByUsername("test@example.com")).thenReturn(userDetails);

    // Set up an admin user
    adminUser = new Usuario();
    adminUser.setEmail("admin@example.com");
    adminUser.setPassword("$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG"); // "password" encoded
    adminUser.setNombre("Admin User");
    adminUser.setRol(Usuario.Role.ADMIN);
    adminDetails = new UsuarioDetalles(adminUser, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    when(usuarioDetallesService.loadUserByUsername("admin@example.com")).thenReturn(adminDetails);

    usuarioDTO = new UsuarioDTO();
    usuarioDTO.setNombre("Test User");
    usuarioDTO.setEmail("test@example.com");
    usuarioDTO.setPassword("password");

    cambioPasswdDTO = new CambioPasswdDTO();
    cambioPasswdDTO.setCurrentPassword("oldPassword");
    cambioPasswdDTO.setNewPassword("newPassword");
    cambioPasswdDTO.setConfirmPassword("newPassword");

    usuarioResponseDTO = new UsuarioResponseDTO(
        1L, "Admin User", "admin@example.com", Usuario.Role.ADMIN, LocalDateTime.now());

    usuarioAdminDTO = new UsuarioAdminDTO();
    usuarioAdminDTO.setNombre("New Admin");
    usuarioAdminDTO.setEmail("newadmin@example.com");
    usuarioAdminDTO.setPassword("adminPassword");
    usuarioAdminDTO.setRol(Usuario.Role.ADMIN);
  }

  @Test
  void mostrarFormularioLogin_ShouldReturnLoginView() throws Exception {
    mockMvc.perform(get("/usuarios/login"))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/login"));
  }

  @Test
  void mostrarFormularioLogin_WithErrorParam_ShouldAddErrorToModel() throws Exception {
    mockMvc.perform(get("/usuarios/login").param("error", "true"))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("loginError"));
  }

  @Test
  void mostrarFormularioLogin_WithRegistroExitosoParam_ShouldAddSuccessToModel() throws Exception {
    mockMvc.perform(get("/usuarios/login").param("registroExitoso", "true"))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("registroExitoso"));
  }

  @Test
  void mostrarFormularioRegistro_ShouldReturnRegistroViewWithUsuarioDTO() throws Exception {
    mockMvc.perform(get("/usuarios/registro"))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/registro"))
        .andExpect(model().attributeExists("usuario"));
  }

  @Test
  void registrarUsuario_WithValidData_ShouldRedirectToLogin() throws Exception {
    mockMvc.perform(post("/usuarios/registro")
        .with(csrf())
        .flashAttr("usuario", usuarioDTO))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/usuarios/login?registroExitoso"));
  }

  @Test
  void registrarUsuario_WithInvalidData_ShouldReturnRegistroView() throws Exception {
    mockMvc.perform(post("/usuarios/registro")
        .with(csrf())
        .param("nombre", "") // Invalid empty name
        .param("email", "invalid-email") // Invalid email
        .param("password", "short")) // Invalid password
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/registro"));
  }

  @Test
  void registrarUsuario_WithExistingEmail_ShouldReturnRegistroViewWithError() throws Exception {
    when(usuarioService.registrarUsuario(any(UsuarioDTO.class)))
        .thenThrow(new EmailAlreadyExistsException("Email already exists"));

    mockMvc.perform(post("/usuarios/registro")
        .with(csrf())
        .flashAttr("usuario", usuarioDTO))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/registro"))
        .andExpect(model().attributeExists("registroError"));
  }

  @Test
  void mostrarPerfil_ShouldReturnPerfilViewWithUserData() throws Exception {

    when(usuarioService.buscarPorEmail(anyString())).thenReturn(testUser);

    mockMvc.perform(get("/usuarios/perfil").with(user(userDetails)))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/perfil"))
        .andExpect(model().attributeExists("usuario"));
  }

  @Test
  void actualizarPerfil_WithValidData_ShouldUpdateProfile() throws Exception {

    mockMvc.perform(post("/usuarios/perfil")
        .with(user(userDetails))
        .with(csrf())
        .flashAttr("usuario",
            new ActualizacionUsuarioDTO("Updated User", "updated@example.com")))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/perfil"))
        .andExpect(model().attribute("toastType", "success"))
        .andExpect(model().attributeExists("toastMessage"));

    verify(usuarioService).actualizarPerfil(anyString(), any(ActualizacionUsuarioDTO.class));
  }

  @Test
  void actualizarPerfil_WithInvalidData_ShouldReturnPerfilView() throws Exception {
    mockMvc.perform(post("/usuarios/perfil")
        .with(user(userDetails))
        .with(csrf())
        .param("nombre", "") // Invalid empty name
        .param("nuevoEmail", "invalid-email")) // Invalid email
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/perfil"));
  }

  @Test
  void actualizarPerfil_WithExistingEmail_ShouldShowError() throws Exception {
    doThrow(new EmailAlreadyExistsException("Email already exists"))
        .when(usuarioService).actualizarPerfil(anyString(), any(ActualizacionUsuarioDTO.class));

    mockMvc.perform(post("/usuarios/perfil")
        .with(user(userDetails))
        .with(csrf())
        .flashAttr("usuario",
            new ActualizacionUsuarioDTO("Updated User", "updated@example.com")))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/perfil"))
        .andExpect(model().attribute("toastType", "danger"))
        .andExpect(model().attributeExists("toastMessage"));
  }

  @Test
  void mostrarFormularioCambioPassword_ShouldReturnPasswordView() throws Exception {
    mockMvc.perform(get("/usuarios/password").with(user(userDetails)))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/password"))
        .andExpect(model().attributeExists("cambioPasswd"));
  }

  @Test
  void cambiarContraseña_WithValidData_ShouldUpdatePassword() throws Exception {
    mockMvc.perform(post("/usuarios/password")
        .with(user(userDetails))
        .with(csrf())
        .flashAttr("cambioPasswd", cambioPasswdDTO))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/password"))
        .andExpect(model().attribute("toastType", "success"))
        .andExpect(model().attributeExists("toastMessage"));

    verify(usuarioService).changePassword(anyString(), any(CambioPasswdDTO.class));
  }

  @Test
  void cambiarContraseña_WithInvalidData_ShouldReturnPasswordView() throws Exception {
    mockMvc.perform(post("/usuarios/password")
        .with(user(userDetails))
        .with(csrf())
        .param("currentPassword", "") // Invalid empty password
        .param("newPassword", "short") // Invalid password
        .param("confirmPassword", "mismatch")) // Mismatch password
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/password"));
  }

  @Test
  void cambiarContraseña_WithInvalidCurrentPassword_ShouldShowError() throws Exception {
    doThrow(new InvalidPasswordException("Invalid current password"))
        .when(usuarioService).changePassword(anyString(), any(CambioPasswdDTO.class));

    mockMvc.perform(post("/usuarios/password")
        .with(user(userDetails))
        .with(csrf())
        .flashAttr("cambioPasswd", cambioPasswdDTO))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/password"))
        .andExpect(model().attribute("toastType", "danger"))
        .andExpect(model().attributeExists("toastMessage"));
  }

  @Test
  void changePassword_WithPasswordMismatch_ShouldShowError() throws Exception {
    doThrow(new PasswordMismatchException("Passwords don't match"))
        .when(usuarioService).changePassword(anyString(), any(CambioPasswdDTO.class));

    mockMvc.perform(post("/usuarios/password")
        .with(user(userDetails))
        .with(csrf())
        .flashAttr("cambioPasswd", cambioPasswdDTO))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/password"))
        .andExpect(model().attribute("toastType", "danger"))
        .andExpect(model().attributeExists("toastMessage"));
  }

  @Test
  @WithMockUser
  void checkAuthentication_WhenAuthenticated_ShouldReturnTrue() throws Exception {
    mockMvc.perform(get("/usuarios/authenticated"))
        .andExpect(status().isOk());
  }

  @Test
  void listarUsuarios_ShouldReturnUserListView() throws Exception {
    List<UsuarioResponseDTO> usuarios = new ArrayList<>();
    usuarios.add(usuarioResponseDTO);

    Page<UsuarioResponseDTO> page = new PageImpl<>(usuarios);
    when(usuarioService.listarUsuarios(anyInt(), anyInt(), anyString(), anyString()))
        .thenReturn(page);

    mockMvc.perform(get("/usuarios/admin/listar").with(user(adminDetails)))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/lista"))
        .andExpect(model().attributeExists("usuarios"))
        .andExpect(model().attributeExists("pagination"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void filtrarUsuarios_ShouldReturnUserPageFragment() throws Exception {
    List<UsuarioResponseDTO> usuarios = new ArrayList<>();
    usuarios.add(usuarioResponseDTO);

    Page<UsuarioResponseDTO> page = new PageImpl<>(usuarios);
    when(usuarioService.listarUsuarios(anyInt(), anyInt(), anyString(), anyString()))
        .thenReturn(page);

    mockMvc.perform(get("/usuarios/admin/filtrar")
        .param("page", "0")
        .param("size", "10")
        .param("sort", "nombre")
        .param("direction", "ASC"))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/lista-usuario-page :: usuario-page"))
        .andExpect(model().attributeExists("usuarios"))
        .andExpect(model().attributeExists("pagination"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void cambiarRol_WithValidId_ShouldReturnUpdatedUserRow() throws Exception {
    when(usuarioService.cambiarRol(any(Long.class))).thenReturn(usuarioResponseDTO);

    mockMvc.perform(post("/usuarios/admin/change-role/1")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/lista-usuario-row :: usuario-row"))
        .andExpect(model().attributeExists("usuario"))
        .andExpect(model().attribute("toastType", "success"))
        .andExpect(model().attributeExists("toastMessage"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void cambiarRol_WithInvalidId_ShouldReturnError() throws Exception {
    when(usuarioService.cambiarRol(any(Long.class)))
        .thenThrow(new ResourceNotFoundException("User not found"));

    mockMvc.perform(post("/usuarios/admin/change-role/999")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("empty :: empty"))
        .andExpect(model().attribute("toastType", "danger"))
        .andExpect(model().attributeExists("toastMessage"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void showAddUserModal_ShouldReturnUserModalFragment() throws Exception {
    mockMvc.perform(get("/usuarios/admin/registrar"))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/nuevo-modal :: userModal"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void createUser_WithValidData_ShouldReturnNewUserRow() throws Exception {
    when(usuarioService.registrarUsuario(any(UsuarioAdminDTO.class))).thenReturn(usuarioResponseDTO);

    mockMvc.perform(post("/usuarios/admin/registrar")
        .with(csrf())
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("nombre", usuarioAdminDTO.getNombre())
        .param("email", usuarioAdminDTO.getEmail())
        .param("password", usuarioAdminDTO.getPassword())
        .param("rol", usuarioAdminDTO.getRol().toString()))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/lista-usuario-row :: usuario-row"))
        .andExpect(model().attributeExists("usuario"))
        .andExpect(model().attribute("toastType", "success"))
        .andExpect(model().attributeExists("toastMessage"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void createUser_WithInvalidData_ShouldReturnError() throws Exception {
    when(usuarioService.registrarUsuario(any(UsuarioAdminDTO.class)))
        .thenThrow(new RuntimeException("Error creating user"));

    mockMvc.perform(post("/usuarios/admin/registrar")
        .with(csrf())
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("nombre", "") // Invalid empty name
        .param("email", "invalid-email") // Invalid email
        .param("password", "short") // Invalid password
        .param("rol", "INVALID")) // Invalid role
        .andExpect(status().isOk())
        .andExpect(view().name("empty :: empty"))
        .andExpect(model().attribute("toastType", "danger"))
        .andExpect(model().attributeExists("toastMessage"));
  }

  @Test
  void listarUsuarios_WithoutAdminRole_ShouldBeForbidden() throws Exception {
    mockMvc.perform(get("/usuarios/admin/listar"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/usuarios/login"));
  }

  @Test
  @WithMockUser
  void listarUsuarios_WithUserRole_ShouldBeForbidden() throws Exception {
    mockMvc.perform(get("/usuarios/admin/listar"))
        .andExpect(status().isForbidden());
  }
}