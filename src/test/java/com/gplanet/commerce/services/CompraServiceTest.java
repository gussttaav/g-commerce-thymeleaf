package com.gplanet.commerce.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.gplanet.commerce.dtos.compra.*;
import com.gplanet.commerce.entities.*;
import com.gplanet.commerce.exceptions.ResourceNotFoundException;
import com.gplanet.commerce.repositories.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CompraServiceTest {

    @Mock
    private CompraMapper compraMapper;
    
    @Mock
    private CompraRepository compraRepository;
    
    @Mock
    private ProductoRepository productoRepository;
    
    @Mock
    private UsuarioRepository usuarioRepository;
    
    @InjectMocks
    private CompraService compraService;
    
    private Usuario adminUser;
    private Usuario regularUser;
    private Producto producto1;
    private Producto producto2;
    private Compra compra;
    private CompraProducto compraProducto1;
    private CompraProducto compraProducto2;
    private CompraResponseDTO compraResponseDTO;
    private CompraDTO compraDTO;
    
    @BeforeEach
    void setUp() {
        // Set up users
        adminUser = new Usuario();
        adminUser.setId(1L);
        adminUser.setNombre("Admin User");
        adminUser.setEmail("admin@example.com");
        adminUser.setRol(Usuario.Role.ADMIN);
        
        regularUser = new Usuario();
        regularUser.setId(2L);
        regularUser.setNombre("Regular User");
        regularUser.setEmail("user@example.com");
        regularUser.setRol(Usuario.Role.USER);
        
        // Set up products
        producto1 = new Producto();
        producto1.setId(1L);
        producto1.setNombre("Product 1");
        producto1.setPrecio(new BigDecimal("10.00"));
        
        producto2 = new Producto();
        producto2.setId(2L);
        producto2.setNombre("Product 2");
        producto2.setPrecio(new BigDecimal("20.00"));
        
        // Set up purchase products
        compraProducto1 = new CompraProducto();
        compraProducto1.setProducto(producto1);
        compraProducto1.setCantidad(2);
        compraProducto1.setSubtotal(new BigDecimal("20.00"));
        
        compraProducto2 = new CompraProducto();
        compraProducto2.setProducto(producto2);
        compraProducto2.setCantidad(1);
        compraProducto2.setSubtotal(new BigDecimal("20.00"));
        
        // Set up purchase
        compra = new Compra();
        compra.setId(1L);
        compra.setUsuario(regularUser);
        compra.setFecha(LocalDateTime.now());
        compra.setTotal(new BigDecimal("40.00"));
        compra.setProductos(Arrays.asList(compraProducto1, compraProducto2));
        
        // Set up DTOs
        compraDTO = new CompraDTO(Arrays.asList(
            new CompraProductoDTO(1L, 2),
            new CompraProductoDTO(2L, 1)
        ));
        
        CompraProductoResponseDTO productoResponseDTO1 = new CompraProductoResponseDTO(
            1L, "Product 1", new BigDecimal("10.00"), 2, new BigDecimal("20.00"));
        
        CompraProductoResponseDTO productoResponseDTO2 = new CompraProductoResponseDTO(
            2L, "Product 2", new BigDecimal("20.00"), 1, new BigDecimal("20.00"));
        
        compraResponseDTO = new CompraResponseDTO(
            1L, 
            "Regular User", 
            compra.getFecha(), 
            new BigDecimal("40.00"), 
            Arrays.asList(productoResponseDTO1, productoResponseDTO2)
        );
    }
    
    @Test
    @DisplayName("Should list all purchases when user is admin")
    void listarCompras_AdminUser_ShouldReturnAllPurchases() {
        // Arrange
        String email = "admin@example.com";
        int page = 0;
        int size = 10;
        String sort = "fecha";
        String direction = "DESC";
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
        Page<Compra> comprasPage = new PageImpl<>(List.of(compra));
        
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(adminUser));
        when(compraRepository.findAll(pageable)).thenReturn(comprasPage);
        when(compraMapper.toCompraResponseDTO(compra)).thenReturn(compraResponseDTO);
        
        // Act
        Page<CompraResponseDTO> result = compraService.listarCompras(email, page, size, sort, direction);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(compraResponseDTO, result.getContent().get(0));
        
        verify(compraRepository).findAll(pageable);
        verify(compraRepository, never()).findByUsuario(any(), any());
    }
    
    @Test
    @DisplayName("Should list only user's purchases when user is not admin")
    void listarCompras_RegularUser_ShouldReturnOnlyUserPurchases() {
        // Arrange
        String email = "user@example.com";
        int page = 0;
        int size = 10;
        String sort = "fecha";
        String direction = "ASC";
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, sort));
        Page<Compra> comprasPage = new PageImpl<>(List.of(compra));
        
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(regularUser));
        when(compraRepository.findByUsuario(regularUser, pageable)).thenReturn(comprasPage);
        when(compraMapper.toCompraResponseDTO(compra)).thenReturn(compraResponseDTO);
        
        // Act
        Page<CompraResponseDTO> result = compraService.listarCompras(email, page, size, sort, direction);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(compraResponseDTO, result.getContent().get(0));
        
        verify(compraRepository, never()).findAll(any(Pageable.class));
        verify(compraRepository).findByUsuario(regularUser, pageable);
    }
    
    @Test
    @DisplayName("Should throw exception when user not found when listing purchases")
    void listarCompras_UserNotFound_ShouldThrowException() {
        // Arrange
        String email = "nonexistent@example.com";
        
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> 
            compraService.listarCompras(email, 0, 10, "fecha", "DESC"));
    }
    
    @Test
    @DisplayName("Should create a purchase successfully")
    void realizarCompra_ValidPurchase_ShouldCreatePurchase() {
        // Arrange
        String email = "user@example.com";
        
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(regularUser));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(producto2));
        when(compraRepository.save(any(Compra.class))).thenAnswer(invocation -> {
            Compra savedCompra = invocation.getArgument(0);
            savedCompra.setId(1L);
            return savedCompra;
        });
        
        // Act
        compraService.realizarCompra(email, compraDTO);
        
        // Assert
        verify(compraRepository).save(argThat(savedCompra -> {
            // Check that the compra was saved with the correct values
            assertEquals(regularUser, savedCompra.getUsuario());
            assertEquals(new BigDecimal("40.00"), savedCompra.getTotal());
            assertEquals(2, savedCompra.getProductos().size());
            
            boolean hasProduct1 = false;
            boolean hasProduct2 = false;
            
            for (CompraProducto cp : savedCompra.getProductos()) {
                if (cp.getProducto().getId().equals(1L)) {
                    hasProduct1 = true;
                    assertEquals(2, cp.getCantidad());
                    assertEquals(new BigDecimal("20.00"), cp.getSubtotal());
                } else if (cp.getProducto().getId().equals(2L)) {
                    hasProduct2 = true;
                    assertEquals(1, cp.getCantidad());
                    assertEquals(new BigDecimal("20.00"), cp.getSubtotal());
                }
            }
            
            return hasProduct1 && hasProduct2;
        }));
    }
    
    @Test
    @DisplayName("Should throw exception when user not found when making a purchase")
    void realizarCompra_UserNotFound_ShouldThrowException() {
        // Arrange
        String email = "nonexistent@example.com";
        
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> 
            compraService.realizarCompra(email, compraDTO));
        
        verify(compraRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should throw exception when product not found when making a purchase")
    void realizarCompra_ProductNotFound_ShouldThrowException() {
        // Arrange
        String email = "user@example.com";
        
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(regularUser));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
        when(productoRepository.findById(2L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            compraService.realizarCompra(email, compraDTO));
        
        verify(compraRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should calculate total correctly for multiple products")
    void realizarCompra_MultipleProducts_ShouldCalculateTotalCorrectly() {
        // Arrange
        String email = "user@example.com";
        
        // Create a purchase with 3 items of product1 and 2 items of product2
        CompraDTO purchaseDto = new CompraDTO(Arrays.asList(
            new CompraProductoDTO(1L, 3),
            new CompraProductoDTO(2L, 2)
        ));
        
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(regularUser));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(producto2));
        when(compraRepository.save(any(Compra.class))).thenAnswer(invocation -> {
            Compra savedCompra = invocation.getArgument(0);
            savedCompra.setId(1L);
            return savedCompra;
        });
        
        // Act
        compraService.realizarCompra(email, purchaseDto);
        
        // Assert
        verify(compraRepository).save(argThat(savedCompra -> {
            // Expected total: (3 * 10.00) + (2 * 20.00) = 30.00 + 40.00 = 70.00
            BigDecimal expectedTotal = new BigDecimal("70.00");
            assertEquals(expectedTotal, savedCompra.getTotal());
            
            return true;
        }));
    }
    
    @Test
    @DisplayName("Should create CompraProducto entities with correct relationships")
    void realizarCompra_ShouldSetupCompraProductoEntitiesCorrectly() {
        // Arrange
        String email = "user@example.com";
        
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(regularUser));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(producto2));
        when(compraRepository.save(any(Compra.class))).thenAnswer(invocation -> {
            Compra savedCompra = invocation.getArgument(0);
            savedCompra.setId(1L);
            return savedCompra;
        });
        
        // Act
        compraService.realizarCompra(email, compraDTO);
        
        // Assert
        verify(compraRepository).save(argThat(savedCompra -> {
            // Verify the relationships are set up correctly
            for (CompraProducto cp : savedCompra.getProductos()) {
                // Verify each CompraProducto has the correct relationships
                assertNotNull(cp.getProducto());
                assertEquals(savedCompra, cp.getCompra());
                
                // Verify the subtotal was calculated correctly
                BigDecimal expectedSubtotal = cp.getProducto().getPrecio()
                    .multiply(BigDecimal.valueOf(cp.getCantidad()));
                assertEquals(expectedSubtotal, cp.getSubtotal());
            }
            
            return true;
        }));
    }
}