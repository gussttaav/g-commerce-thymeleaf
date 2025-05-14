package com.gplanet.commerce.integration;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.gplanet.commerce.dtos.usuario.UsuarioResponseDTO;
import com.gplanet.commerce.entities.Usuario;
import com.gplanet.commerce.repositories.UsuarioRepository;
import com.gplanet.commerce.security.UsuarioDetalles;
import com.gplanet.commerce.security.UsuarioDetallesService;
import com.gplanet.commerce.services.UsuarioService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityIntegrationTest {

  @Autowired
  private WebApplicationContext context;

  private MockMvc mockMvc;

  @MockitoBean
  private UsuarioRepository usuarioRepository;

  @MockitoBean
  private UsuarioService usuarioService;

  @MockitoBean
  private UsuarioDetallesService usuarioDetallesService;

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
  }

  @Test
  @WithAnonymousUser
  public void accessUnsecuredEndpoint_Success() throws Exception {
    mockMvc.perform(get("/"))
        .andExpect(status().isOk());
  }

  @Test
  @WithAnonymousUser
  public void accessSecuredEndpoint_RedirectsToLogin() throws Exception {
    mockMvc.perform(get("/usuarios/perfil"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/usuarios/login"));
  }

  @Test
  public void accessUserEndpoint_AsUser_Success() throws Exception {
    UserDetails userDetails = usuarioDetallesService.loadUserByUsername("user@example.com");
    mockMvc.perform(get("/usuarios/perfil").with(user(userDetails)))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "USER")
  public void accessAdminEndpoint_AsUser_Forbidden() throws Exception {
    mockMvc.perform(get("/usuarios/admin/listar"))
        .andExpect(status().isForbidden());
  }

  @Test
  public void accessAdminEndpoint_AsAdmin_Success() throws Exception {
    // Mock usuario service for admin controller
    List<UsuarioResponseDTO> usuariosList = new ArrayList<>();
    Page<UsuarioResponseDTO> usuariosPage = new PageImpl<>(usuariosList);
    when(usuarioService.listarUsuarios(anyInt(), anyInt(), anyString(), anyString()))
        .thenReturn(usuariosPage);

    UserDetails userDetails = usuarioDetallesService.loadUserByUsername("admin@example.com");
    mockMvc.perform(get("/usuarios/admin/listar").with(user(userDetails)))
        .andExpect(status().isOk());
  }

  @Test
  public void loginWithValidCredentials_Success() throws Exception {
    mockMvc.perform(formLogin("/usuarios/login")
        .user("user@example.com")
        .password("password"))
        .andExpect(authenticated());
  }

  @Test
  public void loginWithInvalidCredentials_Fails() throws Exception {
    mockMvc.perform(formLogin("/usuarios/login")
        .user("user@example.com")
        .password("wrongpassword"))
        .andExpect(unauthenticated());
  }
}