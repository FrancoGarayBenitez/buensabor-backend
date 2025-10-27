package com.elbuensabor.services.mapper;

import com.elbuensabor.dto.request.ArticuloManufacturadoRequestDTO;
import com.elbuensabor.dto.response.ArticuloManufacturadoResponseDTO;
import com.elbuensabor.entities.ArticuloManufacturado;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ArticuloManufacturadoMapper extends BaseMapper<ArticuloManufacturado, ArticuloManufacturadoResponseDTO> {

    // ==================== ENTITY → RESPONSE DTO ====================

    @Override
    @Mapping(source = "unidadMedida.idUnidadMedida", target = "idUnidadMedida")
    @Mapping(source = "unidadMedida.denominacion", target = "denominacionUnidadMedida")

    // Mapear información de categoría al objeto anidado
    @Mapping(target = "categoria", ignore = true) // Se mapea manualmente en el service

    // Campos calculados se asignan en el service
    @Mapping(target = "detalles", ignore = true) // Se mapea manualmente con información completa
    @Mapping(target = "costoTotal", ignore = true)
    @Mapping(target = "margenGanancia", ignore = true)
    @Mapping(target = "cantidadIngredientes", ignore = true)
    @Mapping(target = "stockSuficiente", ignore = true)
    @Mapping(target = "cantidadMaximaPreparable", ignore = true)
    @Mapping(target = "imagenes", ignore = true)
    @Mapping(target = "cantidadVendida", ignore = true)
    ArticuloManufacturadoResponseDTO toDTO(ArticuloManufacturado entity);

    // ==================== REQUEST DTO → ENTITY (CREATE) ====================

    @Mapping(target = "idArticulo", ignore = true)
    @Mapping(target = "unidadMedida", ignore = true) // Se asigna en el service
    @Mapping(target = "categoria", ignore = true)    // Se asigna en el service
    @Mapping(target = "imagenes", ignore = true)     // Se maneja en el service
    @Mapping(target = "detallesPedido", ignore = true)
    @Mapping(target = "promociones", ignore = true)
    @Mapping(target = "detalles", ignore = true)     // Se crean manualmente en el service
    @Mapping(target = "precioVenta", ignore = true)  // Se calcula en el service si no viene
    ArticuloManufacturado toEntity(ArticuloManufacturadoRequestDTO dto);

    // ==================== RESPONSE DTO → ENTITY (GENERIC) ====================

    @Override
    @Mapping(target = "unidadMedida", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "imagenes", ignore = true)
    @Mapping(target = "detallesPedido", ignore = true)
    @Mapping(target = "promociones", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    ArticuloManufacturado toEntity(ArticuloManufacturadoResponseDTO dto);

    // ==================== UPDATE FROM REQUEST DTO ====================

    @Mapping(target = "idArticulo", ignore = true)
    @Mapping(target = "unidadMedida", ignore = true) // Se actualiza en el service
    @Mapping(target = "categoria", ignore = true)    // Se actualiza en el service
    @Mapping(target = "imagenes", ignore = true)
    @Mapping(target = "detallesPedido", ignore = true)
    @Mapping(target = "promociones", ignore = true)
    @Mapping(target = "detalles", ignore = true)     // Se actualiza manualmente en el service
    @Mapping(target = "precioVenta", ignore = true)  // Se recalcula en el service
    void updateEntityFromDTO(ArticuloManufacturadoRequestDTO dto, @MappingTarget ArticuloManufacturado entity);

    // ==================== UPDATE FROM RESPONSE DTO (GENERIC) ====================

    @Override
    @Mapping(target = "idArticulo", ignore = true)
    @Mapping(target = "unidadMedida", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "imagenes", ignore = true)
    @Mapping(target = "detallesPedido", ignore = true)
    @Mapping(target = "promociones", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    void updateEntityFromDTO(ArticuloManufacturadoResponseDTO dto, @MappingTarget ArticuloManufacturado entity);
}