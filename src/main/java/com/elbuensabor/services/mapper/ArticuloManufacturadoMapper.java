package com.elbuensabor.services.mapper;

import com.elbuensabor.dto.request.ArticuloManufacturadoRequestDTO;
import com.elbuensabor.dto.response.ArticuloManufacturadoResponseDTO;
import com.elbuensabor.entities.ArticuloManufacturado;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = { DetalleManufacturadoMapper.class })
public interface ArticuloManufacturadoMapper
        extends BaseMapper<ArticuloManufacturado, ArticuloManufacturadoResponseDTO> {

    // ==================== ENTITY → RESPONSE DTO ====================
    @Override
    @Mapping(source = "unidadMedida.idUnidadMedida", target = "idUnidadMedida")
    @Mapping(source = "unidadMedida.denominacion", target = "denominacionUnidadMedida")
    @Mapping(source = "categoria.idCategoria", target = "idCategoria")
    @Mapping(source = "categoria.denominacion", target = "denominacionCategoria")
    @Mapping(source = "categoria.esSubcategoria", target = "esSubcategoria")
    @Mapping(source = "categoria.categoriaPadre.denominacion", target = "denominacionCategoriaPadre")
    @Mapping(source = "detalles", target = "detalles") // Usa ArticuloManufacturadoDetalleMapper
    @Mapping(target = "imagenes", ignore = true) // Se mapea en el service
    // Campos calculados se asignan en el service
    @Mapping(target = "stockSuficiente", ignore = true)
    @Mapping(target = "cantidadMaximaPreparable", ignore = true)
    ArticuloManufacturadoResponseDTO toDTO(ArticuloManufacturado entity);

    // ==================== REQUEST DTO → ENTITY (CREATE) ====================
    @Mapping(target = "idArticulo", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "unidadMedida", ignore = true) // Se asigna en el service
    @Mapping(target = "categoria", ignore = true) // Se asigna en el service
    @Mapping(target = "imagenes", ignore = true) // Se maneja en el service
    @Mapping(target = "promociones", ignore = true)
    @Mapping(target = "costoProduccion", ignore = true) // Se calcula en el service
    @Mapping(source = "detalles", target = "detalles") // Usa ArticuloManufacturadoDetalleMapper
    ArticuloManufacturado toEntity(ArticuloManufacturadoRequestDTO dto);

    // ==================== UPDATE FROM REQUEST DTO ====================
    @Mapping(target = "idArticulo", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "unidadMedida", ignore = true) // Se actualiza en el service
    @Mapping(target = "categoria", ignore = true) // Se actualiza en el service
    @Mapping(target = "imagenes", ignore = true)
    @Mapping(target = "promociones", ignore = true)
    @Mapping(target = "costoProduccion", ignore = true) // Se recalcula en el service
    @Mapping(target = "detalles", ignore = true) // Se manejan manualmente en el service
    void updateEntityFromDTO(ArticuloManufacturadoRequestDTO dto, @MappingTarget ArticuloManufacturado entity);
}