package com.elbuensabor.services.mapper;

import com.elbuensabor.dto.request.ManufacturadoDetalleDTO;
import com.elbuensabor.entities.ArticuloManufacturadoDetalle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ManufacturadoDetalleMapper {

    // ==================== ENTITY → DTO ====================

    @Mapping(source = "articuloInsumo.idArticulo", target = "idArticuloInsumo")
    @Mapping(source = "articuloInsumo.denominacion", target = "denominacionInsumo")
    @Mapping(source = "articuloInsumo.unidadMedida.denominacion", target = "unidadMedida")
    @Mapping(source = "articuloInsumo.precioCompra", target = "precioCompraUnitario")
    @Mapping(target = "subtotal", ignore = true) // Se calcula en el service (cantidad * precio)
    ManufacturadoDetalleDTO toDTO(ArticuloManufacturadoDetalle entity);

    // ==================== DTO → ENTITY ====================

    @Mapping(target = "idDetalleManufacturado", ignore = true)
    @Mapping(target = "articuloManufacturado", ignore = true) // Se asigna en el service
    @Mapping(target = "articuloInsumo", ignore = true)        // Se asigna en el service
    ArticuloManufacturadoDetalle toEntity(ManufacturadoDetalleDTO dto);
}