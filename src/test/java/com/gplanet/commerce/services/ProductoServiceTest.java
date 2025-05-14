package com.gplanet.commerce.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.gplanet.commerce.dtos.producto.ProductStatus;
import com.gplanet.commerce.dtos.producto.ProductoDTO;
import com.gplanet.commerce.dtos.producto.ProductoMapper;
import com.gplanet.commerce.dtos.producto.ProductoResponseDTO;
import com.gplanet.commerce.entities.Producto;
import com.gplanet.commerce.exceptions.ResourceNotFoundException;
import com.gplanet.commerce.repositories.ProductoRepository;

@ExtendWith(MockitoExtension.class)
public class ProductoServiceTest {

  @Mock
  private ProductoMapper productoMapper;

  @Mock
  private ProductoRepository productoRepository;

  @InjectMocks
  private ProductoService productoService;

  private Producto producto;
  private ProductoDTO productoDTO;
  private ProductoResponseDTO productoResponseDTO;
  private LocalDateTime now;

  @BeforeEach
  void setUp() {
    // Initialize common test data
    now = LocalDateTime.now();

    // Setup test entity
    producto = new Producto();
    producto.setId(1L);
    producto.setNombre("Test Product");
    producto.setDescripcion("Test Description");
    producto.setPrecio(new BigDecimal("99.99"));
    producto.setActivo(true);
    producto.setFechaCreacion(now);

    // Setup DTO
    productoDTO = new ProductoDTO(
        "Test Product",
        "Test Description",
        new BigDecimal("99.99"),
        true);

    // Setup Response DTO
    productoResponseDTO = new ProductoResponseDTO(
        1L,
        "Test Product",
        "Test Description",
        new BigDecimal("99.99"),
        now,
        true);
  }

  @Test
  @DisplayName("Should list active products without search text")
  void listarProductos_ActiveNoSearch_ReturnsActivePage() {
    // Arrange
    Page<Producto> productoPage = new PageImpl<>(List.of(producto));
    when(productoRepository.findByActivoTrue(any(Pageable.class))).thenReturn(productoPage);
    when(productoMapper.toProductoResponseDTO(any(Producto.class))).thenReturn(productoResponseDTO);

    // Act
    Page<ProductoResponseDTO> result = productoService.listarProductos(
        ProductStatus.ACTIVE, "", 0, 10, "nombre", "ASC");

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0)).isEqualTo(productoResponseDTO);
    verify(productoRepository).findByActivoTrue(any(Pageable.class));
  }

  @Test
  @DisplayName("Should list inactive products without search text")
  void listarProductos_InactiveNoSearch_ReturnsInactivePage() {
    // Arrange
    Page<Producto> productoPage = new PageImpl<>(List.of(producto));
    when(productoRepository.findByActivoFalse(any(Pageable.class))).thenReturn(productoPage);
    when(productoMapper.toProductoResponseDTO(any(Producto.class))).thenReturn(productoResponseDTO);

    // Act
    Page<ProductoResponseDTO> result = productoService.listarProductos(
        ProductStatus.INACTIVE, "", 0, 10, "nombre", "ASC");

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    verify(productoRepository).findByActivoFalse(any(Pageable.class));
  }

  @Test
  @DisplayName("Should list all products without search text")
  void listarProductos_AllNoSearch_ReturnsAllPage() {
    // Arrange
    Page<Producto> productoPage = new PageImpl<>(List.of(producto));
    when(productoRepository.findAll(any(Pageable.class))).thenReturn(productoPage);
    when(productoMapper.toProductoResponseDTO(any(Producto.class))).thenReturn(productoResponseDTO);

    // Act
    Page<ProductoResponseDTO> result = productoService.listarProductos(
        ProductStatus.ALL, "", 0, 10, "nombre", "ASC");

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    verify(productoRepository).findAll(any(Pageable.class));
  }

  @Test
  @DisplayName("Should list active products with search text")
  void listarProductos_ActiveWithSearch_ReturnsSearchedActivePage() {
    // Arrange
    Page<Producto> productoPage = new PageImpl<>(List.of(producto));
    when(productoRepository.findByActivoTrueAndSearch(anyString(), any(Pageable.class))).thenReturn(productoPage);
    when(productoMapper.toProductoResponseDTO(any(Producto.class))).thenReturn(productoResponseDTO);

    // Act
    Page<ProductoResponseDTO> result = productoService.listarProductos(
        ProductStatus.ACTIVE, "test", 0, 10, "nombre", "ASC");

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    verify(productoRepository).findByActivoTrueAndSearch(eq("%test%"), any(Pageable.class));
  }

  @Test
  @DisplayName("Should list inactive products with search text")
  void listarProductos_InactiveWithSearch_ReturnsSearchedInactivePage() {
    // Arrange
    Page<Producto> productoPage = new PageImpl<>(List.of(producto));
    when(productoRepository.findByActivoFalseAndSearch(anyString(), any(Pageable.class))).thenReturn(productoPage);
    when(productoMapper.toProductoResponseDTO(any(Producto.class))).thenReturn(productoResponseDTO);

    // Act
    Page<ProductoResponseDTO> result = productoService.listarProductos(
        ProductStatus.INACTIVE, "test", 0, 10, "nombre", "ASC");

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    verify(productoRepository).findByActivoFalseAndSearch(eq("%test%"), any(Pageable.class));
  }

  @Test
  @DisplayName("Should list all products with search text")
  void listarProductos_AllWithSearch_ReturnsSearchedAllPage() {
    // Arrange
    Page<Producto> productoPage = new PageImpl<>(List.of(producto));
    when(productoRepository.findBySearch(anyString(), any(Pageable.class))).thenReturn(productoPage);
    when(productoMapper.toProductoResponseDTO(any(Producto.class))).thenReturn(productoResponseDTO);

    // Act
    Page<ProductoResponseDTO> result = productoService.listarProductos(
        ProductStatus.ALL, "test", 0, 10, "nombre", "ASC");

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    verify(productoRepository).findBySearch(eq("%test%"), any(Pageable.class));
  }

  @Test
  @DisplayName("Should toggle product status from active to inactive")
  void toggleProductStatus_ActiveToInactive_Success() {
    // Arrange
    when(productoRepository.findById(anyLong())).thenReturn(Optional.of(producto));

    Producto updatedProducto = new Producto();
    updatedProducto.setId(1L);
    updatedProducto.setNombre("Test Product");
    updatedProducto.setDescripcion("Test Description");
    updatedProducto.setPrecio(new BigDecimal("99.99"));
    updatedProducto.setActivo(false); // toggled to false
    updatedProducto.setFechaCreacion(now);

    when(productoRepository.save(any(Producto.class))).thenReturn(updatedProducto);

    ProductoResponseDTO updatedResponseDTO = new ProductoResponseDTO(
        1L, "Test Product", "Test Description", new BigDecimal("99.99"), now, false);
    when(productoMapper.toProductoResponseDTO(any(Producto.class))).thenReturn(updatedResponseDTO);

    // Act
    ProductoResponseDTO result = productoService.toggleProductStatus(1L);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.activo()).isFalse();
    verify(productoRepository).findById(1L);
    verify(productoRepository).save(any(Producto.class));
  }

  @Test
  @DisplayName("Should throw exception when toggling non-existent product")
  void toggleProductStatus_NonExistentProduct_ThrowsException() {
    // Arrange
    when(productoRepository.findById(anyLong())).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(ResourceNotFoundException.class, () -> {
      productoService.toggleProductStatus(1L);
    });

    verify(productoRepository).findById(1L);
    verify(productoRepository, times(0)).save(any(Producto.class));
  }

  @Test
  @DisplayName("Should create product successfully")
  void crearProducto_ValidData_ReturnsCreatedProduct() {
    // Arrange
    when(productoMapper.toProducto(any(ProductoDTO.class))).thenReturn(producto);
    when(productoRepository.save(any(Producto.class))).thenReturn(producto);
    when(productoMapper.toProductoResponseDTO(any(Producto.class))).thenReturn(productoResponseDTO);

    // Act
    ProductoResponseDTO result = productoService.crearProducto(productoDTO);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result).isEqualTo(productoResponseDTO);
    verify(productoMapper).toProducto(productoDTO);
    verify(productoRepository).save(any(Producto.class));
    verify(productoMapper).toProductoResponseDTO(producto);
  }

  @Test
  @DisplayName("Should update product successfully")
  void actualizarProducto_ExistingProduct_ReturnsUpdatedProduct() {
    // Arrange
    when(productoRepository.findById(anyLong())).thenReturn(Optional.of(producto));
    when(productoRepository.save(any(Producto.class))).thenReturn(producto);
    when(productoMapper.toProductoResponseDTO(any(Producto.class))).thenReturn(productoResponseDTO);

    // Act
    ProductoResponseDTO result = productoService.actualizarProducto(1L, productoDTO);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result).isEqualTo(productoResponseDTO);
    verify(productoRepository).findById(1L);
    verify(productoMapper).updateProductoFromDTO(productoDTO, producto);
    verify(productoRepository).save(producto);
    verify(productoMapper).toProductoResponseDTO(producto);
  }

  @Test
  @DisplayName("Should throw exception when updating non-existent product")
  void actualizarProducto_NonExistentProduct_ThrowsException() {
    // Arrange
    when(productoRepository.findById(anyLong())).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(ResourceNotFoundException.class, () -> {
      productoService.actualizarProducto(1L, productoDTO);
    });

    verify(productoRepository).findById(1L);
    verify(productoMapper, times(0)).updateProductoFromDTO(any(), any());
    verify(productoRepository, times(0)).save(any());
  }

  @Test
  @DisplayName("Should find product by ID successfully")
  void findById_ExistingProduct_ReturnsProduct() {
    // Arrange
    when(productoRepository.findById(anyLong())).thenReturn(Optional.of(producto));
    when(productoMapper.toProductoResponseDTO(any(Producto.class))).thenReturn(productoResponseDTO);

    // Act
    ProductoResponseDTO result = productoService.findById(1L);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result).isEqualTo(productoResponseDTO);
    verify(productoRepository).findById(1L);
    verify(productoMapper).toProductoResponseDTO(producto);
  }

  @Test
  @DisplayName("Should throw exception when finding non-existent product")
  void findById_NonExistentProduct_ThrowsException() {
    // Arrange
    when(productoRepository.findById(anyLong())).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(ResourceNotFoundException.class, () -> {
      productoService.findById(1L);
    });

    verify(productoRepository).findById(1L);
    verify(productoMapper, times(0)).toProductoResponseDTO(any());
  }
}