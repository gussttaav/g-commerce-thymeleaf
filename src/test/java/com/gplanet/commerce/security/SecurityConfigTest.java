package com.gplanet.commerce.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.gplanet.commerce.dtos.producto.ProductStatus;
import com.gplanet.commerce.dtos.producto.ProductoResponseDTO;
import com.gplanet.commerce.dtos.usuario.UsuarioResponseDTO;
import com.gplanet.commerce.entities.Usuario;
import com.gplanet.commerce.repositories.UsuarioRepository;
import com.gplanet.commerce.services.CompraService;
import com.gplanet.commerce.services.ProductoService;
import com.gplanet.commerce.services.UsuarioService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityConfigTest {

  @Autowired
  private WebApplicationContext context;

  private MockMvc mockMvc;

  @MockitoBean
  private UsuarioRepository usuarioRepository;

  @MockitoBean
  private UsuarioDetallesService usuarioDetallesService;

  @MockitoBean
  private ProductoService productoService;

  @MockitoBean
  private UsuarioService usuarioService;

  @MockitoBean
  private CompraService compraService;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(springSecurity())
        .build();

    // Set up a test user
    Usuario testUser = new Usuario();
    testUser.setEmail("user@example.com");
    testUser.setPassword("$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG"); // "password" encoded
    testUser.setNombre("Test User");
    testUser.setRol(Usuario.Role.USER);

    // Set up an admin user
    Usuario adminUser = new Usuario();
    adminUser.setEmail("admin@example.com");
    adminUser.setPassword("$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG"); // "password" encoded
    adminUser.setNombre("Admin User");
    adminUser.setRol(Usuario.Role.ADMIN);

    when(usuarioRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
    when(usuarioRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));

    when(usuarioService.buscarPorEmail(anyString())).thenReturn(testUser);

    // Mock UserDetailsService to return proper UserDetails objects
    Collection<GrantedAuthority> userAuthorities = new ArrayList<>();
    userAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
    UsuarioDetalles userDetails = new UsuarioDetalles(testUser, userAuthorities);

    Collection<GrantedAuthority> adminAuthorities = new ArrayList<>();
    adminAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
    UsuarioDetalles adminDetails = new UsuarioDetalles(adminUser, adminAuthorities);

    when(usuarioDetallesService.loadUserByUsername("user@example.com")).thenReturn(userDetails);
    when(usuarioDetallesService.loadUserByUsername("admin@example.com")).thenReturn(adminDetails);

    // Mock producto service for the main controller
    List<ProductoResponseDTO> productosList = new ArrayList<>();
    Page<ProductoResponseDTO> productosPage = new PageImpl<>(productosList);
    when(productoService.listarProductos(any(ProductStatus.class), anyString(), anyInt(), anyInt(), anyString(),
        anyString()))
        .thenReturn(productosPage);

    // Mock usuario service for admin controller
    List<UsuarioResponseDTO> usuariosList = new ArrayList<>();
    Page<UsuarioResponseDTO> usuariosPage = new PageImpl<>(usuariosList);
    when(usuarioService.listarUsuarios(anyInt(), anyInt(), anyString(), anyString()))
        .thenReturn(usuariosPage);
  }

  @Test
  public void testPublicEndpoints() throws Exception {
    // Test the public URLs defined in SecurityConfig
    mockMvc.perform(get("/")).andExpect(status().isOk());
    mockMvc.perform(get("/usuarios/registro")).andExpect(status().isOk());
    mockMvc.perform(get("/usuarios/login")).andExpect(status().isOk());
    mockMvc.perform(get("/css/main.css")).andExpect(status().is(404));
    mockMvc.perform(get("/js/app.js")).andExpect(status().is(404));
    mockMvc.perform(get("/img/logo.png")).andExpect(status().is(404));
  }

  @Test
  public void testAuthenticatedEndpoints_Unauthenticated() throws Exception {
    // Test endpoints that require authentication
    mockMvc.perform(get("/usuarios/perfil"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/usuarios/login"));

    mockMvc.perform(get("/usuarios/password"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/usuarios/login"));

    mockMvc.perform(get("/compras/listar"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/usuarios/login"));
  }

  @Test
  public void testAuthenticatedEndpoints_Authenticated() throws Exception {

    UserDetails userDetails = usuarioDetallesService.loadUserByUsername("user@example.com");
    mockMvc.perform(get("/usuarios/perfil").with(user(userDetails)))
        .andExpect(status().isOk());

    mockMvc.perform(get("/usuarios/password").with(user(userDetails)))
        .andExpect(status().isOk());

    when(compraService.listarCompras(anyString(), anyInt(), anyInt(), anyString(), anyString()))
        .thenReturn(new PageImpl<>(new ArrayList<>()));

    mockMvc.perform(get("/compras/listar").with(user(userDetails)))
        .andExpect(status().isOk());
  }

  @Test
  public void testAdminEndpoints_AsUser() throws Exception {

    UserDetails userDetails = usuarioDetallesService.loadUserByUsername("user@example.com");
    mockMvc.perform(get("/usuarios/admin/listar").with(user(userDetails)))
        .andExpect(status().isForbidden());

    mockMvc.perform(get("/productos/admin/listar").with(user(userDetails)))
        .andExpect(status().isForbidden());
  }

  @Test
  public void testAdminEndpoints_AsAdmin() throws Exception {

    UserDetails userDetails = usuarioDetallesService.loadUserByUsername("admin@example.com");
    mockMvc.perform(get("/usuarios/admin/listar").with(user(userDetails)))
        .andExpect(status().isOk());

    mockMvc.perform(get("/productos/admin/listar").with(user(userDetails)))
        .andExpect(status().isOk());
  }

  @Test
  public void testUserOnlyEndpoints_AsUser() throws Exception {
    // Test user-specific endpoints with a regular user
    UserDetails userDetails = usuarioDetallesService.loadUserByUsername("user@example.com");

    mockMvc.perform(post("/compras/nueva")
        .with(user(userDetails))
        .with(csrf())
        .param("productoId", "1")
        .param("cantidad", "1"))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  public void testUserOnlyEndpoints_AsAdmin() throws Exception {
    // Test user-specific endpoints with an admin user (should be denied)
    UserDetails adminDetails = usuarioDetallesService.loadUserByUsername("admin@example.com");

    mockMvc.perform(post("/compras/nueva")
        .with(user(adminDetails))
        .with(csrf())
        .param("productoId", "1")
        .param("cantidad", "1"))
        .andExpect(status().isForbidden());
  }

  @Test
  public void testCsrfProtection() throws Exception {
    UserDetails userDetails = usuarioDetallesService.loadUserByUsername("user@example.com");

    // Attempt to post without CSRF token (should be rejected)
    mockMvc.perform(post("/usuarios/perfil")
        .with(user(userDetails))
        .param("nombre", "Updated Name")
        .param("email", "updated@example.com"))
        .andExpect(status().isForbidden());

    // Now with CSRF token (should be accepted)
    mockMvc.perform(post("/usuarios/perfil")
        .with(user(userDetails))
        .with(csrf())
        .param("nombre", "Updated Name")
        .param("email", "updated@example.com"))
        .andExpect(status().isOk());
  }

  @Test
  public void testApiAuthenticationCheck() throws Exception {
    // Unauthenticated request to authentication check endpoint
    mockMvc.perform(get("/usuarios/authenticated"))
        .andExpect(status().isOk());

    // Authenticated request
    UserDetails userDetails = usuarioDetallesService.loadUserByUsername("user@example.com");
    mockMvc.perform(get("/usuarios/authenticated")
        .with(user(userDetails)))
        .andExpect(status().isOk());
  }
}