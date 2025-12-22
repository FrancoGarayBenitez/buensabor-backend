package com.elbuensabor.services.mapper;

import com.elbuensabor.dto.request.DetalleManufacturadoRequestDTO;
import com.elbuensabor.dto.response.DetalleManufacturadoResponseDTO;
import com.elbuensabor.entities.DetalleManufacturado;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DetalleManufacturadoMapper {
    // ==================== ENTITY → RESPONSE DTO ====================
    @Mapping(source = "idDetalleManufacturado", target = "idDetalleManufacturado")
    @Mapping(source = "cantidad", target = "cantidad")
    @Mapping(source = "articuloInsumo.idArticulo", target = "idArticuloInsumo")
    @Mapping(source = "articuloInsumo.denominacion", target = "denominacionInsumo")
    @Mapping(source = "articuloInsumo.precioCompra", target = "precioCompraInsumo")
    @Mapping(source = "articuloInsumo.unidadMedida.denominacion", target = "unidadMedidaInsumo")
    @Mapping(target = "subtotal", ignore = true) // se calcula en @AfterMapping
    DetalleManufacturadoResponseDTO toDTO(DetalleManufacturado entity);

    // ==================== REQUEST DTO → ENTITY ====================
    @Mapping(target = "idDetalleManufacturado", ignore = true)
    @Mapping(target = "articuloManufacturado", ignore = true) // Se asigna en el service
    @Mapping(target = "articuloInsumo", ignore = true) // Se asigna en el service
    DetalleManufacturado toEntity(DetalleManufacturadoRequestDTO dto);

    // Calcular subtotal = cantidad * precioUnitarioInsumo
    @AfterMapping
    default void calcularSubtotal(DetalleManufacturado entity, @MappingTarget DetalleManufacturadoResponseDTO dto) {
        double cantidad = entity.getCantidad() != null ? entity.getCantidad() : 0.0;
        double precio = (entity.getArticuloInsumo() != null && entity.getArticuloInsumo().getPrecioCompra() != null)
                ? entity.getArticuloInsumo().getPrecioCompra()
                : 0.0;
        dto.setSubtotal(cantidad * precio);
    }
}