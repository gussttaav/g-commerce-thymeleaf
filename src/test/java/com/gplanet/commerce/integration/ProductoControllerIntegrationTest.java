package com.gplanet.commerce.integration;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.gplanet.commerce.entities.Producto;
import com.gplanet.commerce.entities.Usuario;
import com.gplanet.commerce.repositories.ProductoRepository;
import com.gplanet.commerce.repositories.UsuarioRepository;
import com.gplanet.commerce.security.UsuarioDetallesService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProductoControllerIntegrationTest {

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ProductoRepository productoRepository;

  @Autowired
  private UsuarioRepository usuarioRepository;

  @Autowired
  private UsuarioDetallesService usuarioDetallesService;

  private UserDetails adminUser;
  private UserDetails regularUser;
  private Producto testProducto;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(springSecurity())
        .build();

    // Clear repositories
    productoRepository.deleteAll();
    usuarioRepository.deleteAll();

    // Set up admin user
    Usuario adminUsuario = new Usuario();
    adminUsuario.setEmail("admin@example.com");
    adminUsuario.setPassword("$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG"); // "password" encoded
    adminUsuario.setNombre("Admin User");
    adminUsuario.setRol(Usuario.Role.ADMIN);
    usuarioRepository.save(adminUsuario);

    // Set up regular user
    Usuario regularUsuario = new Usuario();
    regularUsuario.setEmail("user@example.com");
    regularUsuario.setPassword("$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG"); // "password" encoded
    regularUsuario.setNombre("Regular User");
    regularUsuario.setRol(Usuario.Role.USER);
    usuarioRepository.save(regularUsuario);

    // Create test product
    testProducto = new Producto();
    testProducto.setNombre("Test Product");
    testProducto.setDescripcion("Test Description");
    testProducto.setPrecio(new BigDecimal("99.99"));
    testProducto.setActivo(true);
    testProducto.setFechaCreacion(LocalDateTime.now());
    testProducto = productoRepository.save(testProducto);

    // Load UserDetails for authentication in tests
    adminUser = usuarioDetallesService.loadUserByUsername("admin@example.com");
    regularUser = usuarioDetallesService.loadUserByUsername("user@example.com");
  }

  @Test
  @DisplayName("Admin should be able to list all products")
  @WithMockUser(roles = "ADMIN")
  public void listarProductosAdmin_ShouldReturnAdminListView() throws Exception {
    mockMvc.perform(get("/productos/admin/listar").with(user(adminUser)))
        .andExpect(status().isOk())
        .andExpect(view().name("productos/lista-admin"))
        .andExpect(model().attributeExists("productos"))
        .andExpect(model().attributeExists("pagination"))
        .andExpect(model().attribute("activePage", "adminProductos"));
  }

  @Test
  @DisplayName("Regular user should not be able to access admin product list")
  @WithMockUser(roles = "USER")
  public void listarProductosAdmin_AsUser_ShouldBeForbidden() throws Exception {
    mockMvc.perform(get("/productos/admin/listar"))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Get product by ID should return product modal for admin")
  public void getProductById_AsAdmin_ShouldReturnProductModal() throws Exception {
    mockMvc.perform(get("/productos/" + testProducto.getId()).with(user(adminUser)))
        .andExpect(status().isOk())
        .andExpect(view().name("productos/producto-modal :: producto-modal"))
        .andExpect(model().attributeExists("producto"));
  }

  @Test
  @DisplayName("Get product by ID should return product modal for admin user")
  public void getProductById_AsAdmiUser_ShouldReturnProductModal() throws Exception {
    mockMvc.perform(get("/productos/" + testProducto.getId()).with(user(adminUser)))
        .andExpect(status().isOk())
        .andExpect(view().name("productos/producto-modal :: producto-modal"))
        .andExpect(model().attributeExists("producto"));
  }

  @Test
  @DisplayName("Get non-existent product should return empty fragment")
  public void getProductById_NonExistent_ShouldReturnEmptyFragment() throws Exception {
    mockMvc.perform(get("/productos/9999").with(user(adminUser)))
        .andExpect(status().isOk())
        .andExpect(view().name("empty :: empty"))
        .andExpect(model().attributeExists("toastMessage"))
        .andExpect(model().attribute("toastType", "danger"));
  }

  @Test
  @DisplayName("Admin should be able to filter products")
  public void filterAdminProducts_ShouldReturnFilteredProducts() throws Exception {
    mockMvc.perform(get("/productos/admin/filtrar")
        .param("search", "Test")
        .param("page", "0")
        .param("size", "10")
        .param("sort", "nombre")
        .param("direction", "ASC")
        .with(user(adminUser)))
        .andExpect(status().isOk())
        .andExpect(view().name("productos/lista-admin-page :: producto-page"))
        .andExpect(model().attributeExists("productos"))
        .andExpect(model().attributeExists("pagination"));
  }

  @Test
  @DisplayName("Regular user should be able to filter active products")
  public void filterProducts_ShouldReturnFilteredProducts() throws Exception {
    mockMvc.perform(get("/productos/filtrar")
        .param("search", "Test")
        .param("page", "0")
        .param("size", "10")
        .param("sort", "nombre")
        .param("direction", "ASC")
        .with(user(regularUser)))
        .andExpect(status().isOk())
        .andExpect(view().name("productos/user-grid :: user-grid"))
        .andExpect(model().attributeExists("productos"))
        .andExpect(model().attributeExists("pagination"));
  }

  @Test
  @DisplayName("Admin should be able to show add product modal")
  public void showAddProductModal_AsAdmin_ShouldReturnProductModal() throws Exception {
    mockMvc.perform(get("/productos/admin/crear").with(user(adminUser)))
        .andExpect(status().isOk())
        .andExpect(view().name("productos/producto-modal :: producto-modal"));
  }

  @Test
  @DisplayName("Regular user should not be able to show add product modal")
  public void showAddProductModal_AsUser_ShouldBeForbidden() throws Exception {
    mockMvc.perform(get("/productos/admin/crear").with(user(regularUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Admin should be able to create a new product")
  public void crearProducto_AsAdmin_ShouldCreateProductAndReturnRow() throws Exception {
    int initialCount = productoRepository.findAll().size();

    mockMvc.perform(post("/productos/admin/crear")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("nombre", "New Product")
        .param("descripcion", "New Product Description")
        .param("precio", "149.99")
        .param("activo", "true")
        .with(csrf())
        .with(user(adminUser)))
        .andExpect(status().isOk())
        .andExpect(view().name("productos/lista-admin-row :: producto-row"))
        .andExpect(model().attributeExists("producto"))
        .andExpect(model().attributeExists("toastMessage"))
        .andExpect(model().attribute("toastType", "success"));

    // Verify product was created in repository
    List<Producto> productos = productoRepository.findAll();
    assertNotNull(productos);
    assertTrue(productos.size() > initialCount);
    assertTrue(productos.stream().anyMatch(p -> p.getNombre().equals("New Product")));
  }

  @Test
  @DisplayName("Admin should be able to update an existing product")
  public void actualizarProducto_AsAdmin_ShouldUpdateProductAndReturnRow() throws Exception {
    mockMvc.perform(post("/productos/admin/actualizar/" + testProducto.getId())
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("nombre", "Updated Product")
        .param("descripcion", "Updated Description")
        .param("precio", "199.99")
        .param("activo", "true")
        .with(csrf())
        .with(user(adminUser)))
        .andExpect(status().isOk())
        .andExpect(view().name("productos/lista-admin-row :: producto-row"))
        .andExpect(model().attributeExists("producto"))
        .andExpect(model().attributeExists("toastMessage"))
        .andExpect(model().attribute("toastType", "success"));

    // Verify product was updated in repository
    Producto updatedProducto = productoRepository.findById(testProducto.getId()).orElse(null);
    assertNotNull(updatedProducto);
    assertTrue(updatedProducto.getNombre().equals("Updated Product"));
    assertTrue(updatedProducto.getDescripcion().equals("Updated Description"));
  }

  @Test
  @DisplayName("Admin should be able to toggle product status")
  public void toggleStatus_AsAdmin_ShouldToggleStatusAndReturnRow() throws Exception {
    boolean initialStatus = testProducto.isActivo();

    mockMvc.perform(post("/productos/admin/toggle-status/" + testProducto.getId())
        .with(csrf())
        .with(user(adminUser)))
        .andExpect(status().isOk())
        .andExpect(view().name("productos/lista-admin-row :: producto-row"))
        .andExpect(model().attributeExists("producto"))
        .andExpect(model().attributeExists("toastMessage"))
        .andExpect(model().attribute("toastType", "success"));

    // Verify status was toggled in repository
    Producto updatedProducto = productoRepository.findById(testProducto.getId()).orElse(null);
    assertNotNull(updatedProducto);
    assertTrue(updatedProducto.isActivo() != initialStatus);
  }

  @Test
  @DisplayName("Regular user should not be able to toggle product status")
  public void toggleStatus_AsUser_ShouldBeForbidden() throws Exception {
    mockMvc.perform(post("/productos/admin/toggle-status/" + testProducto.getId())
        .with(csrf())
        .with(user(regularUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Toggle status for non-existent product should return empty fragment")
  public void toggleStatus_NonExistent_ShouldReturnEmptyFragment() throws Exception {
    mockMvc.perform(post("/productos/admin/toggle-status/9999")
        .with(csrf())
        .with(user(adminUser)))
        .andExpect(status().isOk())
        .andExpect(view().name("empty :: empty"))
        .andExpect(model().attributeExists("toastMessage"))
        .andExpect(model().attribute("toastType", "danger"));
  }

  @Test
  @DisplayName("Update non-existent product should return empty fragment")
  public void actualizarProducto_NonExistent_ShouldReturnEmptyFragment() throws Exception {
    mockMvc.perform(post("/productos/admin/actualizar/9999")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("nombre", "Updated Product")
        .param("descripcion", "Updated Description")
        .param("precio", "199.99")
        .param("activo", "true")
        .with(csrf())
        .with(user(adminUser)))
        .andExpect(status().isOk())
        .andExpect(view().name("empty :: empty"))
        .andExpect(model().attributeExists("toastMessage"))
        .andExpect(model().attribute("toastType", "danger"));
  }

  @Test
  @DisplayName("Unauthenticated access to any product endpoint should redirect to login")
  public void unauthenticatedAccess_ShouldRedirectToLogin() throws Exception {
    mockMvc.perform(get("/productos/admin/listar"))
        .andExpect(status().is3xxRedirection());
  }
}