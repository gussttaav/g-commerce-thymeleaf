package com.gplanet.commerce.integration;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.gplanet.commerce.entities.Usuario;
import com.gplanet.commerce.repositories.UsuarioRepository;
import com.gplanet.commerce.security.UsuarioDetallesService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UsuarioControllerIntegrationTest {

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UsuarioRepository usuarioRepository;

  @Autowired
  private UsuarioDetallesService usuarioDetallesService;

  private static final String TEST_USER_EMAIL = "user@example.com";
  private static final String TEST_USER_PASSWORD = "password";
  private static final String TEST_USER_NAME = "Test User";

  private static final String TEST_ADMIN_EMAIL = "admin@example.com";
  private static final String TEST_ADMIN_NAME = "Test Admin";

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(springSecurity())
        .build();

    // Clear repository
    usuarioRepository.deleteAll();

    // Set up test user with USER role
    Usuario testUser = new Usuario();
    testUser.setEmail(TEST_USER_EMAIL);
    testUser.setPassword("$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG"); // "password" encoded
    testUser.setNombre(TEST_USER_NAME);
    testUser.setRol(Usuario.Role.USER);
    usuarioRepository.save(testUser);

    // Set up test admin with ADMIN role
    Usuario testAdmin = new Usuario();
    testAdmin.setEmail(TEST_ADMIN_EMAIL);
    testAdmin.setPassword("$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG"); // "password" encoded
    testAdmin.setNombre(TEST_ADMIN_NAME);
    testAdmin.setRol(Usuario.Role.ADMIN);
    usuarioRepository.save(testAdmin);
  }

  // Authentication Tests

  @Test
  public void mostrarFormularioLogin_ShouldReturnLoginView() throws Exception {
    mockMvc.perform(get("/usuarios/login"))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/login"));
  }

  @Test
  public void mostrarFormularioLogin_WithErrorParam_ShouldAddErrorAttribute() throws Exception {
    mockMvc.perform(get("/usuarios/login").param("error", "true"))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/login"))
        .andExpect(model().attributeExists("loginError"));
  }

  @Test
  public void mostrarFormularioLogin_WithRegistroExitosoParam_ShouldAddSuccessAttribute() throws Exception {
    mockMvc.perform(get("/usuarios/login").param("registroExitoso", "true"))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/login"))
        .andExpect(model().attributeExists("registroExitoso"));
  }

  @Test
  public void checkAuthentication_WithAuthenticatedUser_ShouldReturnTrue() throws Exception {
    UserDetails userDetails = usuarioDetallesService.loadUserByUsername(TEST_USER_EMAIL);

    mockMvc.perform(get("/usuarios/authenticated").with(user(userDetails)))
        .andExpect(status().isOk())
        .andExpect(content().string("true"));
  }

  @Test
  public void checkAuthentication_WithoutAuthentication_ShouldReturnFalse() throws Exception {
    mockMvc.perform(get("/usuarios/authenticated"))
        .andExpect(status().isOk())
        .andExpect(content().string("false"));
  }

  // Registration Tests

  @Test
  public void mostrarFormularioRegistro_ShouldReturnRegistroView() throws Exception {
    mockMvc.perform(get("/usuarios/registro"))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/registro"))
        .andExpect(model().attributeExists("usuario"));
  }

  @Test
  public void registrarUsuario_WithValidData_ShouldRedirectToLogin() throws Exception {
    mockMvc.perform(post("/usuarios/registro")
        .param("nombre", "New User")
        .param("email", "new@example.com")
        .param("password", "password")
        .param("confirmPassword", "password")
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/usuarios/login?registroExitoso"));

    // Verify user was created in repository
    assertTrue(usuarioRepository.existsByEmail("new@example.com"));
  }

  @Test
  public void registrarUsuario_WithExistingEmail_ShouldReturnRegistroViewWithError() throws Exception {
    mockMvc.perform(post("/usuarios/registro")
        .param("nombre", "Duplicate User")
        .param("email", TEST_USER_EMAIL) // Using existing email
        .param("password", "password")
        .param("confirmPassword", "password")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/registro"))
        .andExpect(model().attributeExists("registroError"));
  }

  @Test
  public void registrarUsuario_WithInvalidData_ShouldReturnRegistroViewWithBindingErrors() throws Exception {
    mockMvc.perform(post("/usuarios/registro")
        .param("nombre", "") // Empty name is invalid
        .param("email", "invalid-email") // Invalid email format
        .param("password", "pass") // Password might be too short
        .param("confirmPassword", "different") // Doesn't match password
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/registro"))
        .andExpect(model().hasErrors());
  }

  // Profile Tests

  @Test
  public void mostrarPerfil_AuthenticatedUser_ShouldReturnProfileView() throws Exception {
    UserDetails userDetails = usuarioDetallesService.loadUserByUsername(TEST_USER_EMAIL);

    mockMvc.perform(get("/usuarios/perfil").with(user(userDetails)))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/perfil"))
        .andExpect(model().attributeExists("usuario"));
  }

  @Test
  public void actualizarPerfil_WithValidData_ShouldUpdateProfile() throws Exception {
    UserDetails userDetails = usuarioDetallesService.loadUserByUsername(TEST_USER_EMAIL);

    mockMvc.perform(post("/usuarios/perfil")
        .with(user(userDetails))
        .param("nombre", "Updated Name")
        .param("nuevoEmail", TEST_USER_EMAIL) // Same email, no conflict
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/perfil"));

    // Verify name was updated
    Usuario updatedUser = usuarioRepository.findByEmail(TEST_USER_EMAIL).orElseThrow();
    assertEquals("Updated Name", updatedUser.getNombre());
  }

  @Test
  public void actualizarPerfil_WithExistingEmail_ShouldReturnErrorToast() throws Exception {
    UserDetails userDetails = usuarioDetallesService.loadUserByUsername(TEST_USER_EMAIL);

    mockMvc.perform(post("/usuarios/perfil")
        .with(user(userDetails))
        .param("nombre", "Updated Name")
        .param("nuevoEmail", TEST_ADMIN_EMAIL) // Using admin's email will cause conflict
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/perfil"))
        .andExpect(model().attributeExists("toastMessage"))
        .andExpect(model().attribute("toastType", "danger"));
  }

  // Password Change Tests

  @Test
  public void mostrarFormularioCambioPassword_AuthenticatedUser_ShouldReturnPasswordView() throws Exception {
    UserDetails userDetails = usuarioDetallesService.loadUserByUsername(TEST_USER_EMAIL);

    mockMvc.perform(get("/usuarios/password").with(user(userDetails)))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/password"))
        .andExpect(model().attributeExists("cambioPasswd"));
  }

  @Test
  public void cambiarContraseña_WithValidData_ShouldChangePassword() throws Exception {
    UserDetails userDetails = usuarioDetallesService.loadUserByUsername(TEST_USER_EMAIL);

    mockMvc.perform(post("/usuarios/password")
        .with(user(userDetails))
        .param("currentPassword", TEST_USER_PASSWORD)
        .param("newPassword", "newPassword123")
        .param("confirmPassword", "newPassword123")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/password"))
        .andExpect(model().attributeExists("toastMessage"))
        .andExpect(model().attribute("toastType", "success"));
  }

  @Test
  public void cambiarContraseña_WithIncorrectCurrentPassword_ShouldReturnErrorToast() throws Exception {
    UserDetails userDetails = usuarioDetallesService.loadUserByUsername(TEST_USER_EMAIL);

    mockMvc.perform(post("/usuarios/password")
        .with(user(userDetails))
        .param("currentPassword", "wrongPassword")
        .param("newPassword", "newPassword123")
        .param("confirmPassword", "newPassword123")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/password"))
        .andExpect(model().attributeExists("toastMessage"))
        .andExpect(model().attribute("toastType", "danger"));
  }

  @Test
  public void cambiarContraseña_WithMismatchingPasswords_ShouldReturnErrorToast() throws Exception {
    UserDetails userDetails = usuarioDetallesService.loadUserByUsername(TEST_USER_EMAIL);

    mockMvc.perform(post("/usuarios/password")
        .with(user(userDetails))
        .param("currentPassword", TEST_USER_PASSWORD)
        .param("newPassword", "newPassword123")
        .param("confirmPassword", "differentPassword")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/password"))
        .andExpect(model().attributeExists("toastMessage"))
        .andExpect(model().attribute("toastType", "danger"));
  }

  @Test
  public void cambiarContraseña_WithSameAsOldPassword_ShouldReturnErrorToast() throws Exception {
    UserDetails userDetails = usuarioDetallesService.loadUserByUsername(TEST_USER_EMAIL);

    mockMvc.perform(post("/usuarios/password")
        .with(user(userDetails))
        .param("currentPassword", TEST_USER_PASSWORD)
        .param("newPassword", TEST_USER_PASSWORD) // Same as current
        .param("confirmPassword", TEST_USER_PASSWORD)
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/password"))
        .andExpect(model().attributeExists("toastMessage"))
        .andExpect(model().attribute("toastType", "danger"));
  }

  // Admin User Management Tests

  @Test
  public void listarUsuarios_AsAdmin_ShouldReturnListaView() throws Exception {
    UserDetails adminDetails = usuarioDetallesService.loadUserByUsername(TEST_ADMIN_EMAIL);

    mockMvc.perform(get("/usuarios/admin/listar").with(user(adminDetails)))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/lista"))
        .andExpect(model().attributeExists("usuarios"))
        .andExpect(model().attributeExists("pagination"))
        .andExpect(model().attribute("activePage", "adminUsuarios"));
  }

  @Test
  public void listarUsuarios_AsRegularUser_ShouldBeDenied() throws Exception {
    UserDetails userDetails = usuarioDetallesService.loadUserByUsername(TEST_USER_EMAIL);

    mockMvc.perform(get("/usuarios/admin/listar").with(user(userDetails)))
        .andExpect(status().isForbidden());
  }

  @Test
  public void filtrarUsuarios_AsAdmin_ShouldReturnUserListFragment() throws Exception {
    UserDetails adminDetails = usuarioDetallesService.loadUserByUsername(TEST_ADMIN_EMAIL);

    mockMvc.perform(get("/usuarios/admin/filtrar")
        .with(user(adminDetails))
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
  public void cambiarRol_AsAdmin_ShouldToggleUserRole() throws Exception {
    UserDetails adminDetails = usuarioDetallesService.loadUserByUsername(TEST_ADMIN_EMAIL);

    // Get user ID
    Usuario testUser = usuarioRepository.findByEmail(TEST_USER_EMAIL).orElseThrow();
    Long userId = testUser.getId();

    // Initial role is USER - should change to ADMIN
    mockMvc.perform(post("/usuarios/admin/change-role/" + userId)
        .with(user(adminDetails))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/lista-usuario-row :: usuario-row"))
        .andExpect(model().attributeExists("usuario"))
        .andExpect(model().attributeExists("toastMessage"))
        .andExpect(model().attribute("toastType", "success"));

    // Verify role was changed
    Usuario updatedUser = usuarioRepository.findById(userId).orElseThrow();
    assertEquals(Usuario.Role.ADMIN, updatedUser.getRol());

    // Change back to USER
    mockMvc.perform(post("/usuarios/admin/change-role/" + userId)
        .with(user(adminDetails))
        .with(csrf()))
        .andExpect(status().isOk());

    // Verify role was changed back
    updatedUser = usuarioRepository.findById(userId).orElseThrow();
    assertEquals(Usuario.Role.USER, updatedUser.getRol());
  }

  @Test
  public void cambiarRol_WithInvalidId_ShouldReturnErrorToast() throws Exception {
    UserDetails adminDetails = usuarioDetallesService.loadUserByUsername(TEST_ADMIN_EMAIL);

    mockMvc.perform(post("/usuarios/admin/change-role/9999") // Invalid ID
        .with(user(adminDetails))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("empty :: empty"))
        .andExpect(model().attributeExists("toastMessage"))
        .andExpect(model().attribute("toastType", "danger"));
  }

  @Test
  public void showAddUserModal_AsAdmin_ShouldReturnModalFragment() throws Exception {
    UserDetails adminDetails = usuarioDetallesService.loadUserByUsername(TEST_ADMIN_EMAIL);

    mockMvc.perform(get("/usuarios/admin/registrar")
        .with(user(adminDetails)))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/nuevo-modal :: userModal"));
  }

  @Test
  public void createUser_AsAdmin_WithValidData_ShouldAddNewUser() throws Exception {
    UserDetails adminDetails = usuarioDetallesService.loadUserByUsername(TEST_ADMIN_EMAIL);

    mockMvc.perform(post("/usuarios/admin/registrar")
        .with(user(adminDetails))
        .param("nombre", "Admin Created User")
        .param("email", "adminCreated@example.com")
        .param("password", "password123")
        .param("rol", "USER")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("usuarios/lista-usuario-row :: usuario-row"))
        .andExpect(model().attributeExists("usuario"))
        .andExpect(model().attributeExists("toastMessage"))
        .andExpect(model().attribute("toastType", "success"));

    // Verify user was created
    assertTrue(usuarioRepository.existsByEmail("adminCreated@example.com"));
  }

  @Test
  public void createUser_AsAdmin_WithInvalidData_ShouldReturnErrorToast() throws Exception {
    UserDetails adminDetails = usuarioDetallesService.loadUserByUsername(TEST_ADMIN_EMAIL);

    mockMvc.perform(post("/usuarios/admin/registrar")
        .with(user(adminDetails))
        .param("nombre", "") // Empty name is invalid
        .param("email", "invalid-email") // Invalid email format
        .param("password", "")
        .param("rol", "USER")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("empty :: empty"))
        .andExpect(model().attributeExists("toastMessage"))
        .andExpect(model().attribute("toastType", "danger"));
  }

  // Security Tests

  @Test
  public void accessSecuredEndpoints_WithoutAuthentication_ShouldRedirectToLogin() throws Exception {
    // Test profile page
    mockMvc.perform(get("/usuarios/perfil"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));

    // Test password page
    mockMvc.perform(get("/usuarios/password"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  public void accessAdminEndpoints_AsRegularUser_ShouldBeDenied() throws Exception {
    UserDetails userDetails = usuarioDetallesService.loadUserByUsername(TEST_USER_EMAIL);

    mockMvc.perform(get("/usuarios/admin/listar").with(user(userDetails)))
        .andExpect(status().isForbidden());

    mockMvc.perform(get("/usuarios/admin/filtrar").with(user(userDetails)))
        .andExpect(status().isForbidden());

    // Get any user ID for role change test
    Usuario anyUser = usuarioRepository.findByEmail(TEST_USER_EMAIL).orElseThrow();

    mockMvc.perform(post("/usuarios/admin/change-role/" + anyUser.getId())
        .with(user(userDetails))
        .with(csrf()))
        .andExpect(status().isForbidden());
  }
}