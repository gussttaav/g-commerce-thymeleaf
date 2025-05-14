package com.gplanet.commerce.integration;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.transaction.annotation.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.gplanet.commerce.entities.Compra;
import com.gplanet.commerce.entities.CompraProducto;
import com.gplanet.commerce.entities.Producto;
import com.gplanet.commerce.entities.Usuario;
import com.gplanet.commerce.repositories.CompraRepository;
import com.gplanet.commerce.repositories.ProductoRepository;
import com.gplanet.commerce.repositories.UsuarioRepository;
import com.gplanet.commerce.security.UsuarioDetallesService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CompraControllerIntegrationTest {

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UsuarioRepository usuarioRepository;

  @Autowired
  private ProductoRepository productoRepository;

  @Autowired
  private CompraRepository compraRepository;

  @Autowired
  private UsuarioDetallesService usuarioDetallesService;

  private Usuario testUser;
  private Usuario adminUser;
  private Producto testProduct;
  private Producto secondProduct;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(springSecurity())
        .build();

    setupTestData();
  }

  @Transactional
  public void setupTestData() {
    compraRepository.deleteAll();
    compraRepository.flush(); // Ensure deletes are committed

    usuarioRepository.deleteAll();
    usuarioRepository.flush();

    productoRepository.deleteAll();
    productoRepository.flush();

    // Set up test user
    testUser = new Usuario();
    testUser.setEmail("user@example.com");
    testUser.setPassword("$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG"); // "password" encoded
    testUser.setNombre("Test User");
    testUser.setRol(Usuario.Role.USER);
    testUser = usuarioRepository.saveAndFlush(testUser);

    // Set up admin user
    adminUser = new Usuario();
    adminUser.setEmail("admin@example.com");
    adminUser.setPassword("$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG"); // "password" encoded
    adminUser.setNombre("Admin User");
    adminUser.setRol(Usuario.Role.ADMIN);
    adminUser = usuarioRepository.saveAndFlush(adminUser);

    // Set up test products
    testProduct = new Producto();
    testProduct.setNombre("Test Product");
    testProduct.setPrecio(BigDecimal.valueOf(10.0));
    testProduct.setActivo(true);
    testProduct = productoRepository.saveAndFlush(testProduct);

    secondProduct = new Producto();
    secondProduct.setNombre("Second Product");
    secondProduct.setPrecio(BigDecimal.valueOf(15.0));
    secondProduct.setActivo(true);
    secondProduct = productoRepository.saveAndFlush(secondProduct);
  }

  @Test
  public void listarCompras_AuthenticatedUser_ShouldReturnPurchaseListView() throws Exception {
    UserDetails userDetails = usuarioDetallesService.loadUserByUsername("user@example.com");

    mockMvc.perform(get("/compras/listar").with(user(userDetails)))
        .andExpect(status().isOk())
        .andExpect(view().name("compras/lista"))
        .andExpect(model().attributeExists("compras"))
        .andExpect(model().attributeExists("pagination"))
        .andExpect(model().attribute("activePage", "compras"));
  }

  @Test
  @Transactional
  public void processPurchase_WithValidData_ShouldRedirectToHome() throws Exception {
    UserDetails userDetails = usuarioDetallesService.loadUserByUsername("user@example.com");

    mockMvc.perform(post("/compras/nueva")
        .param("productos[0].productoId", testProduct.getId().toString())
        .param("productos[0].cantidad", "2")
        .with(user(userDetails))
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/?compraExitosa=true"));

    // Verify purchase was created
    Page<Compra> comprasPage = compraRepository.findByUsuario(testUser, PageRequest.of(0, 10));
    assertEquals(1, comprasPage.getContent().size());
    assertEquals(0, BigDecimal.valueOf(20.0).compareTo(comprasPage.getContent().get(0).getTotal()));
  }

  @Test
  @Transactional
  public void processPurchase_WithMultipleProducts_ShouldCalculateTotalCorrectly() throws Exception {
    UserDetails userDetails = usuarioDetallesService.loadUserByUsername("user@example.com");

    mockMvc.perform(post("/compras/nueva")
        .param("productos[0].productoId", testProduct.getId().toString())
        .param("productos[0].cantidad", "2")
        .param("productos[1].productoId", secondProduct.getId().toString())
        .param("productos[1].cantidad", "1")
        .with(user(userDetails))
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/?compraExitosa=true"));

    // Verify purchase was created with correct total
    Page<Compra> comprasPage = compraRepository.findByUsuario(testUser, PageRequest.of(0, 10));
    assertEquals(1, comprasPage.getContent().size());

    Compra compra = comprasPage.getContent().get(0);
    assertEquals(0, BigDecimal.valueOf(35.0).compareTo(compra.getTotal())); // 2*10 + 1*15 = 35
    assertEquals(2, compra.getProductos().size());
  }

  @Test
  public void processPurchase_WithInvalidProductId_ShouldRedirectWithError() throws Exception {
    UserDetails userDetails = usuarioDetallesService.loadUserByUsername("user@example.com");

    mockMvc.perform(post("/compras/nueva")
        .param("productos[0].productoId", "999") // Non-existent product ID
        .param("productos[0].cantidad", "2")
        .with(user(userDetails))
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/?compraExitosa=false"));
  }

  @Test
  public void processPurchase_WithInvalidQuantity_ShouldRedirectWithError() throws Exception {
    UserDetails userDetails = usuarioDetallesService.loadUserByUsername("user@example.com");

    mockMvc.perform(post("/compras/nueva")
        .param("productos[0].productoId", testProduct.getId().toString())
        .param("productos[0].cantidad", "0") // Invalid quantity
        .with(user(userDetails))
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/?compraExitosa=false"));
  }

  @Test
  public void processPurchase_Unauthenticated_ShouldRedirectToLogin() throws Exception {
    mockMvc.perform(post("/compras/nueva")
        .param("productos[0].productoId", testProduct.getId().toString())
        .param("productos[0].cantidad", "2")
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/usuarios/login"));
  }

  @Test
  public void listarCompras_Unauthenticated_ShouldRedirectToLogin() throws Exception {
    mockMvc.perform(get("/compras/listar"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/usuarios/login"));
  }

  @Test
  @Transactional
  public void listarCompras_WithPurchaseHistory_ShouldDisplayCorrectData() throws Exception {
    // Create a purchase in the database
    Compra compra = new Compra();
    compra.setUsuario(testUser);
    compra.setFecha(LocalDateTime.now());
    compra.setTotal(BigDecimal.valueOf(30.0));

    CompraProducto compraProducto = new CompraProducto();
    compraProducto.setProducto(testProduct);
    compraProducto.setCantidad(3);
    compraProducto.setSubtotal(BigDecimal.valueOf(30.0));
    compra.addCompraProducto(compraProducto);

    compraRepository.save(compra);

    UserDetails userDetails = usuarioDetallesService.loadUserByUsername("user@example.com");

    mockMvc.perform(get("/compras/listar").with(user(userDetails)))
        .andExpect(status().isOk())
        .andExpect(view().name("compras/lista"))
        .andExpect(model().attributeExists("compras"))
        .andExpect(model().attribute("compras", hasSize(1)));
  }

  @Test
  @Transactional
  public void filterProducts_ShouldReturnPaginatedResults() throws Exception {
    // Create multiple purchases
    for (int i = 0; i < 15; i++) {
      Compra compra = new Compra();
      compra.setUsuario(testUser);
      compra.setFecha(LocalDateTime.now().minusDays(i));
      compra.setTotal(BigDecimal.valueOf(10.0 * (i + 1)));

      CompraProducto compraProducto = new CompraProducto();
      compraProducto.setProducto(testProduct);
      compraProducto.setCantidad(i + 1);
      compraProducto.setSubtotal(BigDecimal.valueOf(10.0 * (i + 1)));
      compra.addCompraProducto(compraProducto);

      compraRepository.save(compra);
    }

    UserDetails userDetails = usuarioDetallesService.loadUserByUsername("user@example.com");

    // Test first page
    mockMvc.perform(get("/compras/filtrar")
        .param("page", "0")
        .param("size", "5")
        .with(user(userDetails)))
        .andExpect(status().isOk())
        .andExpect(view().name("compras/page :: compras-page"))
        .andExpect(model().attribute("compras", hasSize(5)))
        .andExpect(model().attributeExists("pagination"));

    // Test second page
    mockMvc.perform(get("/compras/filtrar")
        .param("page", "1")
        .param("size", "5")
        .with(user(userDetails)))
        .andExpect(status().isOk())
        .andExpect(view().name("compras/page :: compras-page"))
        .andExpect(model().attribute("compras", hasSize(5)));
  }

  @Test
  @Transactional
  public void filterProducts_WithDifferentSortOrder_ShouldReturnSortedResults() throws Exception {
    // Create purchases with different dates
    for (int i = 0; i < 5; i++) {
      Compra compra = new Compra();
      compra.setUsuario(testUser);
      compra.setFecha(LocalDateTime.now().minusDays(i));
      compra.setTotal(BigDecimal.valueOf(10.0 * (i + 1)));

      CompraProducto compraProducto = new CompraProducto();
      compraProducto.setProducto(testProduct);
      compraProducto.setCantidad(i + 1);
      compraProducto.setSubtotal(BigDecimal.valueOf(10.0 * (i + 1)));
      compra.addCompraProducto(compraProducto);

      compraRepository.save(compra);
    }

    UserDetails userDetails = usuarioDetallesService.loadUserByUsername("user@example.com");

    // Test descending order (default)
    MvcResult descResult = mockMvc.perform(get("/compras/filtrar")
        .param("sort", "total")
        .param("direction", "DESC")
        .with(user(userDetails)))
        .andExpect(status().isOk())
        .andReturn();

    // Test ascending order
    MvcResult ascResult = mockMvc.perform(get("/compras/filtrar")
        .param("sort", "total")
        .param("direction", "ASC")
        .with(user(userDetails)))
        .andExpect(status().isOk())
        .andReturn();

    // Results should be different when sorted in different directions
    assertNotEquals(descResult.getResponse().getContentAsString(),
        ascResult.getResponse().getContentAsString());
  }

  @Test
  @Transactional
  public void listarCompras_AsAdmin_ShouldShowAllPurchases() throws Exception {
    // Create purchases for regular user
    for (int i = 0; i < 3; i++) {
      Compra compra = new Compra();
      compra.setUsuario(testUser);
      compra.setFecha(LocalDateTime.now());
      compra.setTotal(BigDecimal.valueOf(10.0));
      compraRepository.save(compra);
    }

    // Create purchases for admin user
    for (int i = 0; i < 2; i++) {
      Compra compra = new Compra();
      compra.setUsuario(adminUser);
      compra.setFecha(LocalDateTime.now());
      compra.setTotal(BigDecimal.valueOf(20.0));
      compraRepository.save(compra);
    }

    UserDetails adminDetails = usuarioDetallesService.loadUserByUsername("admin@example.com");

    // Admin should see all purchases (both users)
    mockMvc.perform(get("/compras/listar").with(user(adminDetails)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("compras", hasSize(5)));

    UserDetails userDetails = usuarioDetallesService.loadUserByUsername("user@example.com");

    // Regular user should only see their own purchases
    mockMvc.perform(get("/compras/listar").with(user(userDetails)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("compras", hasSize(3)));
  }

  @Test
  public void processPurchase_WithoutCsrf_ShouldBeForbidden() throws Exception {
    UserDetails userDetails = usuarioDetallesService.loadUserByUsername("user@example.com");

    mockMvc.perform(post("/compras/nueva")
        .param("productos[0].productoId", testProduct.getId().toString())
        .param("productos[0].cantidad", "2")
        .with(user(userDetails)))
        // No CSRF token
        .andExpect(status().isForbidden());
  }
}