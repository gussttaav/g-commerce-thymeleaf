package com.gplanet.commerce.dtos.producto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.gplanet.commerce.entities.Producto;

/**
 * Mapper interface for converting between Product entities and DTOs.
 * This interface uses MapStruct to automatically generate the implementation
 * of the mapping methods between different product representations.
 *
 * @author Gustavo
 * @version 1.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductoMapper {
    
    /**
     * Converts a Product entity to a ProductoResponseDTO.
     *
     * @param producto the product entity to convert
     * @return the corresponding ProductoResponseDTO
     */
    ProductoResponseDTO toProductoResponseDTO(Producto producto);

    /**
     * Converts a ProductoDTO to a Product entity.
     * The id and fechaCreacion fields are ignored during the mapping.
     *
     * @param productoDTO the product DTO to convert
     * @return the corresponding Product entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    Producto toProducto(ProductoDTO productoDTO);

    /**
     * Updates an existing Product entity with data from a ProductoDTO.
     * Preserves the id and fechaCreacion fields of the existing entity.
     *
     * @param productoDTO the source DTO containing updated data
     * @param producto the target Product entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    void updateProductoFromDTO(ProductoDTO productoDTO, @MappingTarget Producto producto);
}