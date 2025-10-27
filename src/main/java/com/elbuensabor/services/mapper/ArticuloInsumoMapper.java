package com.elbuensabor.services.mapper;

import com.elbuensabor.dto.request.ArticuloInsumoRequestDTO;
import com.elbuensabor.dto.response.ArticuloInsumoResponseDTO;
import com.elbuensabor.entities.ArticuloInsumo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ArticuloInsumoMapper extends BaseMapper<ArticuloInsumo, ArticuloInsumoResponseDTO> {

    // ==================== ENTITY → RESPONSE DTO ====================

    @Override
    @Mapping(source = "unidadMedida.idUnidadMedida", target = "idUnidadMedida")
    @Mapping(source = "unidadMedida.denominacion", target = "denominacionUnidadMedida")

    @Mapping(source = "categoria.idCategoria", target = "idCategoria")
    @Mapping(source = "categoria.denominacion", target = "denominacionCategoria")
    @Mapping(source = "categoria.esSubcategoria", target = "esSubcategoria")
    @Mapping(source = "categoria.categoriaPadre.denominacion", target = "denominacionCategoriaPadre")

    // Campos calculados se asignan en el service
    @Mapping(target = "porcentajeStock", ignore = true)
    @Mapping(target = "estadoStock", ignore = true)
    @Mapping(target = "stockDisponible", source = "stockActual")
    @Mapping(target = "cantidadProductosQueLoUsan", ignore = true)
    @Mapping(target = "imagenes", ignore = true) // Se mapea en el service
    ArticuloInsumoResponseDTO toDTO(ArticuloInsumo entity);

    // ==================== REQUEST DTO → ENTITY (CREATE) ====================

    @Mapping(target = "idArticulo", ignore = true)
    @Mapping(target = "unidadMedida", ignore = true) // Se asigna en el service
    @Mapping(target = "categoria", ignore = true)    // Se asigna en el service
    @Mapping(target = "imagenes", ignore = true)     // Se maneja en el service
    @Mapping(target = "detallesPedido", ignore = true)
    @Mapping(target = "promociones", ignore = true)
    @Mapping(target = "detallesManufacturados", ignore = true)
    ArticuloInsumo toEntity(ArticuloInsumoRequestDTO dto);

    // ==================== RESPONSE DTO → ENTITY (GENERIC) ====================

    @Override
    @Mapping(target = "unidadMedida", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "imagenes", ignore = true)
    @Mapping(target = "detallesPedido", ignore = true)
    @Mapping(target = "promociones", ignore = true)
    @Mapping(target = "detallesManufacturados", ignore = true)
    ArticuloInsumo toEntity(ArticuloInsumoResponseDTO dto);

    // ==================== UPDATE FROM REQUEST DTO ====================

    @Mapping(target = "idArticulo", ignore = true)
    @Mapping(target = "unidadMedida", ignore = true) // Se actualiza en el service
    @Mapping(target = "categoria", ignore = true)    // Se actualiza en el service
    @Mapping(target = "imagenes", ignore = true)
    @Mapping(target = "detallesPedido", ignore = true)
    @Mapping(target = "promociones", ignore = true)
    @Mapping(target = "detallesManufacturados", ignore = true)
    void updateEntityFromDTO(ArticuloInsumoRequestDTO dto, @MappingTarget ArticuloInsumo entity);

    // ==================== UPDATE FROM RESPONSE DTO (GENERIC) ====================

    @Override
    @Mapping(target = "idArticulo", ignore = true)
    @Mapping(target = "unidadMedida", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "imagenes", ignore = true)
    @Mapping(target = "detallesPedido", ignore = true)
    @Mapping(target = "promociones", ignore = true)
    @Mapping(target = "detallesManufacturados", ignore = true)
    void updateEntityFromDTO(ArticuloInsumoResponseDTO dto, @MappingTarget ArticuloInsumo entity);
}