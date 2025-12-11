package com.elbuensabor.services.mapper;

import com.elbuensabor.dto.request.DetalleManufacturadoRequestDTO;
import com.elbuensabor.dto.response.DetalleManufacturadoResponseDTO;
import com.elbuensabor.entities.DetalleManufacturado;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DetalleManufacturadoMapper {
    // ==================== ENTITY → RESPONSE DTO ====================
    @Mapping(source = "idDetalleManufacturado", target = "idDetalleManufacturado")
    @Mapping(source = "cantidad", target = "cantidad")
    @Mapping(source = "articuloInsumo.idArticulo", target = "idArticuloInsumo")
    @Mapping(source = "articuloInsumo.denominacion", target = "denominacionInsumo")
    @Mapping(source = "articuloInsumo.unidadMedida.denominacion", target = "unidadMedidaInsumo")
    @Mapping(source = "articuloInsumo.precioCompra", target = "costoInsumo")
    DetalleManufacturadoResponseDTO toDTO(DetalleManufacturado entity);

    // ==================== REQUEST DTO → ENTITY ====================
    @Mapping(target = "idDetalleManufacturado", ignore = true)
    @Mapping(target = "articuloManufacturado", ignore = true) // Se asigna en el service
    @Mapping(target = "articuloInsumo", ignore = true) // Se asigna en el service
    DetalleManufacturado toEntity(DetalleManufacturadoRequestDTO dto);
}