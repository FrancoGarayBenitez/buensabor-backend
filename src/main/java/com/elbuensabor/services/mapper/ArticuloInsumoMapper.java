package com.elbuensabor.services.mapper;

import com.elbuensabor.dto.request.ArticuloInsumoRequestDTO;
import com.elbuensabor.dto.response.ArticuloInsumoResponseDTO;
import com.elbuensabor.entities.ArticuloInsumo;
import org.mapstruct.*;

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
    @Mapping(source = "esParaElaborar", target = "esParaElaborar")

    // ✅ CAMPOS CALCULADOS EN SERVICE (ignorados en mapper)
    @Mapping(target = "porcentajeStock", ignore = true)
    @Mapping(target = "estadoStock", ignore = true)
    @Mapping(target = "stockDisponible", source = "stockActual")
    @Mapping(target = "costoTotalInventario", ignore = true)
    @Mapping(target = "margenGanancia", ignore = true)
    @Mapping(target = "cantidadProductosQueLoUsan", ignore = true)
    ArticuloInsumoResponseDTO toDTO(ArticuloInsumo entity);

    // ==================== REQUEST DTO → ENTITY (CREATE) ====================

    @Mapping(target = "idArticulo", ignore = true)
    @Mapping(target = "eliminado", constant = "false")
    @Mapping(target = "estadoStock", constant = "CRITICO")

    // ✅ RELACIONES MANEJADAS EN SERVICE
    @Mapping(target = "unidadMedida", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "detallesPromocion", ignore = true)
    @Mapping(target = "imagenes", ignore = true)

    // ✅ RELACIONES INVERSA DE ARTICULOINSUMO
    @Mapping(target = "detallesManufacturados", ignore = true)
    @Mapping(target = "historicosPrecios", ignore = true)
    @Mapping(target = "compras", ignore = true)
    ArticuloInsumo toEntity(ArticuloInsumoRequestDTO dto);

    // ==================== UPDATE FROM REQUEST DTO ====================

    @Mapping(target = "idArticulo", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "estadoStock", ignore = true)

    // ✅ RELACIONES MANEJADAS EN SERVICE
    @Mapping(target = "unidadMedida", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "detallesPromocion", ignore = true)
    @Mapping(target = "imagenes", ignore = true)

    // ✅ RELACIONES INVERSA: NO SE ACTUALIZAN DIRECTAMENTE
    @Mapping(target = "detallesManufacturados", ignore = true)
    @Mapping(target = "historicosPrecios", ignore = true)
    @Mapping(target = "compras", ignore = true)
    void updateEntityFromDTO(ArticuloInsumoRequestDTO dto, @MappingTarget ArticuloInsumo entity);

    // ==================== RESPONSE DTO → ENTITY (GENERIC) ====================

    @Override
    @Mapping(target = "idArticulo", ignore = true)
    @Mapping(target = "unidadMedida", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "detallesPromocion", ignore = true)
    @Mapping(target = "imagenes", ignore = true)
    @Mapping(target = "detallesManufacturados", ignore = true)
    @Mapping(target = "historicosPrecios", ignore = true)
    @Mapping(target = "compras", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "estadoStock", ignore = true)
    ArticuloInsumo toEntity(ArticuloInsumoResponseDTO dto);

    // ==================== UPDATE FROM RESPONSE DTO (GENERIC) ====================

    @Override
    @Mapping(target = "idArticulo", ignore = true)
    @Mapping(target = "unidadMedida", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "detallesPromocion", ignore = true)
    @Mapping(target = "imagenes", ignore = true)
    @Mapping(target = "detallesManufacturados", ignore = true)
    @Mapping(target = "historicosPrecios", ignore = true)
    @Mapping(target = "compras", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "estadoStock", ignore = true)
    void updateEntityFromDTO(ArticuloInsumoResponseDTO dto, @MappingTarget ArticuloInsumo entity);
}