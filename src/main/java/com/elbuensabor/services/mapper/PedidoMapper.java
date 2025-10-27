package com.elbuensabor.services.mapper;

import com.elbuensabor.dto.request.PedidoRequestDTO;
import com.elbuensabor.dto.response.PedidoResponseDTO;
import com.elbuensabor.entities.Pedido;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {DetallePedidoMapper.class, DomicilioMapper.class})
public interface PedidoMapper extends BaseMapper<Pedido, PedidoResponseDTO> {

    // ==================== ENTITY → RESPONSE DTO ====================
    @Override
    @Mapping(source = "cliente.idCliente", target = "idCliente")
    @Mapping(source = "cliente.usuario.nombre", target = "nombreCliente")
    @Mapping(source = "cliente.usuario.apellido", target = "apellidoCliente")
    @Mapping(source = "cliente.telefono", target = "telefonoCliente")
    @Mapping(source = "estado", target = "estado")
    @Mapping(source = "tipoEnvio", target = "tipoEnvio")
    @Mapping(source = "domicilio", target = "domicilio")
    @Mapping(source = "detalles", target = "detalles")
    // ✅ NUEVO: Mapeo explícito de observaciones (aunque debería ser automático)
    @Mapping(source = "observaciones", target = "observaciones")
    @Mapping(target = "stockSuficiente", ignore = true) // Se calcula en el service
    @Mapping(target = "tiempoEstimadoTotal", ignore = true) // Se calcula en el service
    PedidoResponseDTO toDTO(Pedido entity);

    // ==================== REQUEST DTO → ENTITY ====================
    @Mapping(target = "idPedido", ignore = true)
    @Mapping(target = "fecha", ignore = true) // Se asigna en el service
    @Mapping(target = "horaEstimadaFinalizacion", ignore = true) // Se calcula en el service
    @Mapping(target = "total", ignore = true) // Se calcula en el service
    @Mapping(target = "totalCosto", ignore = true) // Se calcula en el service
    @Mapping(target = "estado", ignore = true) // Se asigna en el service
    @Mapping(target = "cliente", ignore = true) // Se asigna en el service
    @Mapping(target = "domicilio", ignore = true) // Se asigna en el service
    @Mapping(target = "sucursal", ignore = true) // Se asigna en el service
    @Mapping(target = "detalles", ignore = true) // Se crean en el service
    @Mapping(target = "factura", ignore = true)
    // ✅ NUEVO: Mapeo de observaciones desde request
    @Mapping(source = "observaciones", target = "observaciones")
    Pedido toEntity(PedidoRequestDTO dto);

    // ==================== RESPONSE DTO → ENTITY (GENERIC) ====================
    @Override
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "domicilio", ignore = true)
    @Mapping(target = "sucursal", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    @Mapping(target = "factura", ignore = true)

    @Mapping(target = "totalCosto", ignore = true)  // ← AGREGADO para quitar warning


    // ✅ NUEVO: Incluir observaciones en mapeo genérico
    @Mapping(source = "observaciones", target = "observaciones")

    Pedido toEntity(PedidoResponseDTO dto);

    // ==================== UPDATE FROM DTO ====================
    @Override
    @Mapping(target = "idPedido", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "domicilio", ignore = true)
    @Mapping(target = "sucursal", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    @Mapping(target = "factura", ignore = true)

    @Mapping(target = "totalCosto", ignore = true)  // ← AGREGADO para quitar warning


    // ✅ NUEVO: Permitir actualización de observaciones
    @Mapping(source = "observaciones", target = "observaciones")

    void updateEntityFromDTO(PedidoResponseDTO dto, @MappingTarget Pedido entity);
}