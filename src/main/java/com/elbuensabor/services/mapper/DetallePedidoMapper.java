package com.elbuensabor.services.mapper;

import com.elbuensabor.dto.request.DetallePedidoRequestDTO;
import com.elbuensabor.dto.response.DetallePedidoResponseDTO;
import com.elbuensabor.entities.DetallePedido;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DetallePedidoMapper {

    // ==================== ENTITY → RESPONSE DTO ====================
    @Mapping(source = "articulo.idArticulo", target = "idArticulo")
    @Mapping(source = "articulo.denominacion", target = "denominacionArticulo")
    @Mapping(source = "articulo.precioVenta", target = "precioUnitario")
    @Mapping(source = "articulo.unidadMedida.denominacion", target = "unidadMedida")
    @Mapping(target = "tiempoPreparacion", expression = "java(calcularTiempoPreparacion(entity.getArticulo()))")
    @Mapping(source = "observaciones", target = "observaciones")

    // ✅ NUEVOS: Campos de promociones
    @Mapping(source = "precioUnitarioOriginal", target = "precioUnitarioOriginal")
    @Mapping(source = "descuentoPromocion", target = "descuentoPromocion")
    @Mapping(target = "precioUnitarioFinal", expression = "java(calcularPrecioUnitarioFinal(entity))")
    @Mapping(target = "tienePromocion", expression = "java(entity.getPromocionAplicada() != null && entity.getDescuentoPromocion() != null && entity.getDescuentoPromocion() > 0)")
    @Mapping(target = "promocionAplicada", expression = "java(mapPromocionAplicada(entity.getPromocionAplicada()))")
    DetallePedidoResponseDTO toDTO(DetallePedido entity);

    // ==================== REQUEST DTO → ENTITY ====================
    @Mapping(target = "idDetallePedido", ignore = true)
    @Mapping(target = "subtotal", ignore = true) // Se calcula en el service
    @Mapping(target = "articulo", ignore = true) // Se asigna en el service
    @Mapping(target = "pedido", ignore = true) // Se asigna en el service
    @Mapping(source = "observaciones", target = "observaciones")

    // ✅ NUEVOS: Campos de promociones (se asignan en el service)
    @Mapping(target = "precioUnitarioOriginal", ignore = true)
    @Mapping(target = "descuentoPromocion", ignore = true)
    @Mapping(target = "promocionAplicada", ignore = true)
    DetallePedido toEntity(DetallePedidoRequestDTO dto);

    // ==================== MÉTODOS AUXILIARES ====================

    // ✅ TU MÉTODO EXISTENTE - MANTENIDO
    default Integer calcularTiempoPreparacion(com.elbuensabor.entities.Articulo articulo) {
        if (articulo instanceof com.elbuensabor.entities.ArticuloManufacturado) {
            com.elbuensabor.entities.ArticuloManufacturado manufacturado = (com.elbuensabor.entities.ArticuloManufacturado) articulo;
            return manufacturado.getTiempoEstimadoEnMinutos();
        }
        return 0; // Los insumos no tienen tiempo de preparación
    }

    // ✅ NUEVO: Calcular precio unitario final con descuento
    default Double calcularPrecioUnitarioFinal(DetallePedido entity) {
        if (entity.getPrecioUnitarioOriginal() == null || entity.getCantidad() == null || entity.getCantidad() == 0) {
            return entity.getArticulo() != null ? entity.getArticulo().getPrecioVenta() : 0.0;
        }

        Double descuento = entity.getDescuentoPromocion() != null ? entity.getDescuentoPromocion() : 0.0;
        return entity.getPrecioUnitarioOriginal() - (descuento / entity.getCantidad());
    }

    // ✅ NUEVO: Mapear información de promoción aplicada
    default DetallePedidoResponseDTO.PromocionAplicadaDTO mapPromocionAplicada(
            com.elbuensabor.entities.Promocion promocion) {
        if (promocion == null) {
            return null;
        }

        DetallePedidoResponseDTO.PromocionAplicadaDTO dto = new DetallePedidoResponseDTO.PromocionAplicadaDTO();
        dto.setIdPromocion(promocion.getIdPromocion());
        dto.setDenominacion(promocion.getDenominacion());
        dto.setDescripcion(promocion.getDescripcionDescuento());
        dto.setTipoDescuento(promocion.getTipoDescuento().toString());
        dto.setValorDescuento(promocion.getValorDescuento());

        return dto;
    }
}