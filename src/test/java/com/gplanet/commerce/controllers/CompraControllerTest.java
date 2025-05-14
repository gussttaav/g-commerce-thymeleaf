package com.gplanet.commerce.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.gplanet.commerce.dtos.compra.CompraDTO;
import com.gplanet.commerce.dtos.compra.CompraProductoDTO;
import com.gplanet.commerce.dtos.compra.CompraResponseDTO;
import com.gplanet.commerce.entities.Usuario;
import com.gplanet.commerce.security.SecurityConfig;
import com.gplanet.commerce.security.UsuarioDetalles;
import com.gplanet.commerce.security.UsuarioDetallesService;
import com.gplanet.commerce.services.CompraService;
import com.gplanet.commerce.exceptions.ResourceNotFoundException;

@WebMvcTest(CompraController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CompraControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private CompraService compraService;

  @MockitoBean
  private UsuarioDetallesService usuarioDetallesService;

  private Usuario testUser;
  private UsuarioDetalles userDetails;
  private CompraDTO compraDTO;
  private CompraResponseDTO compraResponseDTO;

  @BeforeEach
  void setUp() {
    // Set up a test user
    testUser = new Usuario();
    testUser.setEmail("test@example.com");
    testUser.setPassword("$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG"); // "password" encoded
    testUser.setNombre("Test User");
    testUser.setRol(Usuario.Role.USER);
    userDetails = new UsuarioDetalles(testUser, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    when(usuarioDetallesService.loadUserByUsername("test@example.com")).thenReturn(userDetails);

    // Set up test purchase data
    compraDTO = new CompraDTO(List.of(
        new CompraProductoDTO(1L, 2),
        new CompraProductoDTO(2L, 1)));

    compraResponseDTO = new CompraResponseDTO(
        1L,
        "Test User",
        LocalDateTime.now(),
        new BigDecimal("100.00"),
        List.of());
  }

  @Test
  @WithMockUser(roles = "USER")
  void processPurchase_WithValidData_ShouldRedirectWithSuccess() throws Exception {
    mockMvc.perform(post("/compras/nueva")
        .with(csrf())
        .flashAttr("compraDTO", compraDTO))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/?compraExitosa=true"));

    verify(compraService).realizarCompra(anyString(), any(CompraDTO.class));
  }

  @Test
  @WithMockUser(roles = "USER")
  void processPurchase_WithInvalidData_ShouldRedirectWithError() throws Exception {
    CompraDTO invalidCompraDTO = new CompraDTO(List.of(
        new CompraProductoDTO(null, 0) // Invalid product ID and quantity
    ));

    mockMvc.perform(post("/compras/nueva")
        .with(csrf())
        .flashAttr("compraDTO", invalidCompraDTO))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/?compraExitosa=false"));
  }

  @Test
  @WithMockUser(roles = "USER")
  void processPurchase_WithServiceError_ShouldRedirectWithError() throws Exception {
    doThrow(new ResourceNotFoundException("Product not found"))
        .when(compraService).realizarCompra(anyString(), any(CompraDTO.class));

    mockMvc.perform(post("/compras/nueva")
        .with(csrf())
        .flashAttr("compraDTO", compraDTO))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/?compraExitosa=false"));
  }

  @Test
  void processPurchase_WithoutAuthentication_ShouldRedirectToLogin() throws Exception {
    mockMvc.perform(post("/compras/nueva")
        .with(csrf())
        .flashAttr("compraDTO", compraDTO))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/usuarios/login"));
  }

  @Test
  @WithMockUser(roles = "USER")
  void listarCompras_ShouldReturnPurchaseListView() throws Exception {
    Page<CompraResponseDTO> page = new PageImpl<>(List.of(compraResponseDTO));
    when(compraService.listarCompras(anyString(), anyInt(), anyInt(), anyString(), anyString()))
        .thenReturn(page);

    mockMvc.perform(get("/compras/listar")
        .with(user(userDetails)))
        .andExpect(status().isOk())
        .andExpect(view().name("compras/lista"))
        .andExpect(model().attributeExists("compras"))
        .andExpect(model().attributeExists("pagination"))
        .andExpect(model().attribute("activePage", "compras"));
  }

  @Test
  void listarCompras_WithoutAuthentication_ShouldRedirectToLogin() throws Exception {
    mockMvc.perform(get("/compras/listar"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/usuarios/login"));
  }

  @Test
  @WithMockUser(roles = "USER")
  void filterPurchases_ShouldReturnPurchasePageFragment() throws Exception {
    Page<CompraResponseDTO> page = new PageImpl<>(List.of(compraResponseDTO));
    when(compraService.listarCompras(anyString(), anyInt(), anyInt(), anyString(), anyString()))
        .thenReturn(page);

    mockMvc.perform(get("/compras/filtrar")
        .param("page", "0")
        .param("size", "10")
        .param("sort", "fecha")
        .param("direction", "DESC")
        .with(user(userDetails)))
        .andExpect(status().isOk())
        .andExpect(view().name("compras/page :: compras-page"))
        .andExpect(model().attributeExists("compras"))
        .andExpect(model().attributeExists("pagination"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void filterPurchases_AsAdmin_ShouldReturnAllPurchases() throws Exception {
    Page<CompraResponseDTO> page = new PageImpl<>(List.of(compraResponseDTO));
    when(compraService.listarCompras(anyString(), anyInt(), anyInt(), anyString(), anyString()))
        .thenReturn(page);

    mockMvc.perform(get("/compras/filtrar")
        .param("page", "0")
        .param("size", "10")
        .param("sort", "fecha")
        .param("direction", "DESC"))
        .andExpect(status().isOk());
  }
}