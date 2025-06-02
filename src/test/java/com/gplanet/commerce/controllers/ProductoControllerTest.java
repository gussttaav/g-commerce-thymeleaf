package com.gplanet.commerce.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
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

import com.gplanet.commerce.dtos.producto.ProductStatus;
import com.gplanet.commerce.dtos.producto.ProductoDTO;
import com.gplanet.commerce.dtos.producto.ProductoResponseDTO;
import com.gplanet.commerce.entities.Usuario;
import com.gplanet.commerce.exceptions.ProductCreationException;
import com.gplanet.commerce.exceptions.ResourceNotFoundException;
import com.gplanet.commerce.security.SecurityConfig;
import com.gplanet.commerce.security.UsuarioDetalles;
import com.gplanet.commerce.security.UsuarioDetallesService;
import com.gplanet.commerce.services.ProductoService;

@WebMvcTest(ProductoController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductoControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext context;

  @MockitoBean
  private ProductoService productoService;

  @MockitoBean
  private UsuarioDetallesService usuarioDetallesService;

  private ProductoDTO productoDTO;
  private ProductoResponseDTO productoResponseDTO;
  private Usuario adminUser;
  private UsuarioDetalles adminDetails;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(springSecurity())
        .build();

    // Set up an admin user
    adminUser = new Usuario();
    adminUser.setEmail("admin@example.com");
    adminUser.setPassword("$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG"); // "password" encoded
    adminUser.setNombre("Admin User");
    adminUser.setRol(Usuario.Role.ADMIN);
    adminDetails = new UsuarioDetalles(adminUser, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    when(usuarioDetallesService.loadUserByUsername("admin@example.com")).thenReturn(adminDetails);

    productoDTO = new ProductoDTO(
        "Test Product",
        "Test Description",
        BigDecimal.valueOf(19.99),
        true);

    productoResponseDTO = new ProductoResponseDTO(
        1L,
        "Test Product",
        "Test Description",
        BigDecimal.valueOf(19.99),
        LocalDateTime.now(),
        true);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void listarProductos_ShouldReturnProductListView() throws Exception {
    List<ProductoResponseDTO> productos = new ArrayList<>();
    productos.add(productoResponseDTO);

    Page<ProductoResponseDTO> page = new PageImpl<>(productos);
    when(productoService.listarProductos(any(ProductStatus.class), anyString(), anyInt(), anyInt(), anyString(),
        anyString()))
        .thenReturn(page);

    mockMvc.perform(get("/productos/admin/listar").with(user(adminDetails)))
        .andExpect(status().isOk())
        .andExpect(view().name("productos/lista-admin"))
        .andExpect(model().attributeExists("productos"))
        .andExpect(model().attributeExists("pagination"))
        .andExpect(model().attribute("activePage", "adminProductos"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void toggleStatus_WithValidId_ShouldReturnUpdatedProductRow() throws Exception {
    when(productoService.toggleProductStatus(anyLong())).thenReturn(productoResponseDTO);

    mockMvc.perform(post("/productos/admin/toggle-status/1")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("productos/lista-admin-row :: producto-row"))
        .andExpect(model().attributeExists("producto"))
        .andExpect(model().attribute("toastType", "success"))
        .andExpect(model().attributeExists("toastMessage"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void toggleStatus_WithInvalidId_ShouldReturnError() throws Exception {
    when(productoService.toggleProductStatus(anyLong()))
        .thenThrow(new ResourceNotFoundException("Product not found"));

    mockMvc.perform(post("/productos/admin/toggle-status/999")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("empty :: empty"))
        .andExpect(model().attribute("toastType", "danger"))
        .andExpect(model().attributeExists("toastMessage"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void getProductById_WithValidId_ShouldReturnProductModal() throws Exception {
    when(productoService.findById(anyLong())).thenReturn(productoResponseDTO);

    mockMvc.perform(get("/productos/1"))
        .andExpect(status().isOk())
        .andExpect(view().name("productos/producto-modal :: producto-modal"))
        .andExpect(model().attributeExists("producto"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void getProductById_WithInvalidId_ShouldReturnError() throws Exception {
    when(productoService.findById(anyLong()))
        .thenThrow(new ResourceNotFoundException("Product not found"));

    mockMvc.perform(get("/productos/999"))
        .andExpect(status().isOk())
        .andExpect(view().name("empty :: empty"))
        .andExpect(model().attribute("toastType", "danger"))
        .andExpect(model().attributeExists("toastMessage"));
  }

  @Test
  void filterProducts_ShouldReturnUserGridFragment() throws Exception {
    List<ProductoResponseDTO> productos = new ArrayList<>();
    productos.add(productoResponseDTO);

    Page<ProductoResponseDTO> page = new PageImpl<>(productos);
    when(productoService.listarProductos(any(ProductStatus.class), anyString(), anyInt(), anyInt(), anyString(),
        anyString()))
        .thenReturn(page);

    mockMvc.perform(get("/productos/filtrar")
        .param("page", "0")
        .param("size", "10")
        .param("search", "")
        .param("sort", "nombre")
        .param("direction", "ASC"))
        .andExpect(status().isOk())
        .andExpect(view().name("productos/user-grid :: user-grid"))
        .andExpect(model().attributeExists("productos"))
        .andExpect(model().attributeExists("pagination"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void filterAdminProducts_ShouldReturnProductPageFragment() throws Exception {
    List<ProductoResponseDTO> productos = new ArrayList<>();
    productos.add(productoResponseDTO);

    Page<ProductoResponseDTO> page = new PageImpl<>(productos);
    when(productoService.listarProductos(any(ProductStatus.class), anyString(), anyInt(), anyInt(), anyString(),
        anyString()))
        .thenReturn(page);

    mockMvc.perform(get("/productos/admin/filtrar")
        .param("status", "ALL")
        .param("page", "0")
        .param("size", "10")
        .param("search", "")
        .param("sort", "nombre")
        .param("direction", "ASC"))
        .andExpect(status().isOk())
        .andExpect(view().name("productos/lista-admin-page :: producto-page"))
        .andExpect(model().attributeExists("productos"))
        .andExpect(model().attributeExists("pagination"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void showAddProductModal_ShouldReturnProductModalFragment() throws Exception {
    mockMvc.perform(get("/productos/admin/crear"))
        .andExpect(status().isOk())
        .andExpect(view().name("productos/producto-modal :: producto-modal"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void crearProducto_WithValidData_ShouldReturnNewProductRow() throws Exception {
    when(productoService.crearProducto(any(ProductoDTO.class))).thenReturn(productoResponseDTO);

    mockMvc.perform(post("/productos/admin/crear")
        .with(csrf())
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("nombre", productoDTO.nombre())
        .param("descripcion", productoDTO.descripcion())
        .param("precio", "19.99")
        .param("activo", "true"))
        .andExpect(status().isOk())
        .andExpect(view().name("productos/lista-admin-row :: producto-row"))
        .andExpect(model().attributeExists("producto"))
        .andExpect(model().attribute("toastType", "success"))
        .andExpect(model().attributeExists("toastMessage"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void crearProducto_WithValidationErrors_ShouldReturnBadRequest() throws Exception {
    mockMvc.perform(post("/productos/admin/crear")
        .with(csrf())
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("nombre", "") // Invalid empty name
        .param("descripcion", "Test")
        .param("precio", "0") // Invalid price
        .param("activo", "true"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void crearProducto_WithInvalidData_ShouldReturnError() throws Exception {
    // Create invalid data that will pass validation but fail in service
    ProductoDTO invalidProduct = new ProductoDTO(
        "Valid Name", // This will pass @NotBlank validation
        "Test Description",
        BigDecimal.valueOf(19.99),
        true);

    when(productoService.crearProducto(any(ProductoDTO.class)))
        .thenThrow(new ProductCreationException("Error creating product"));

    mockMvc.perform(post("/productos/admin/crear")
        .with(csrf())
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("nombre", invalidProduct.nombre())
        .param("descripcion", invalidProduct.descripcion())
        .param("precio", invalidProduct.precio().toString())
        .param("activo", String.valueOf(invalidProduct.activo())))
        .andExpect(status().isOk())
        .andExpect(view().name("empty :: empty"))
        .andExpect(model().attribute("toastType", "danger"))
        .andExpect(model().attributeExists("toastMessage"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void actualizarProducto_WithValidData_ShouldReturnUpdatedProductRow() throws Exception {
    when(productoService.actualizarProducto(anyLong(), any(ProductoDTO.class))).thenReturn(productoResponseDTO);

    mockMvc.perform(post("/productos/admin/actualizar/1")
        .with(csrf())
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("nombre", productoDTO.nombre())
        .param("descripcion", productoDTO.descripcion())
        .param("precio", "19.99")
        .param("activo", "true"))
        .andExpect(status().isOk())
        .andExpect(view().name("productos/lista-admin-row :: producto-row"))
        .andExpect(model().attributeExists("producto"))
        .andExpect(model().attribute("toastType", "success"))
        .andExpect(model().attributeExists("toastMessage"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void actualizarProducto_WithInvalidId_ShouldReturnError() throws Exception {
    when(productoService.actualizarProducto(anyLong(), any(ProductoDTO.class)))
        .thenThrow(new ResourceNotFoundException("Product not found"));

    mockMvc.perform(post("/productos/admin/actualizar/999")
        .with(csrf())
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("nombre", productoDTO.nombre())
        .param("descripcion", productoDTO.descripcion())
        .param("precio", "19.99")
        .param("activo", "true"))
        .andExpect(status().isOk())
        .andExpect(view().name("empty :: empty"))
        .andExpect(model().attribute("toastType", "danger"))
        .andExpect(model().attributeExists("toastMessage"));
  }

  @Test
  void listarProductos_WithoutAdminRole_ShouldBeForbidden() throws Exception {
    mockMvc.perform(get("/productos/admin/listar"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/usuarios/login"));
  }

  @Test
  @WithMockUser
  void listarProductos_WithUserRole_ShouldBeForbidden() throws Exception {
    mockMvc.perform(get("/productos/admin/listar"))
        .andExpect(status().isForbidden());
  }
}