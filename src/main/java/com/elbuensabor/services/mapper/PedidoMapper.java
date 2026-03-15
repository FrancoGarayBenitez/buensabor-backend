package com.elbuensabor.services.mapper;

import com.elbuensabor.dto.request.pedido.CrearPedidoRequest;
import com.elbuensabor.dto.response.pedido.*;
import com.elbuensabor.entities.Pedido;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", uses = { DetallePedidoMapper.class, DomicilioMapper.class })
public interface PedidoMapper {

    // ========== TO RESPONSE - ADMIN ==========

    @Mappings({
            @Mapping(source = "idPedido", target = "idPedido"),
            @Mapping(source = "fecha", target = "fecha"),
            @Mapping(source = "estado", target = "estado"),

            // Cliente - Obtenemos datos del Cliente y su Usuario asociado
            @Mapping(source = "cliente.idCliente", target = "idCliente"),
            @Mapping(expression = "java(getNombreCompletoCliente(entity))", target = "nombreCliente"),
            @Mapping(source = "cliente.usuario.email", target = "emailCliente"),
            @Mapping(source = "cliente.telefono", target = "telefonoCliente"),

            @Mapping(source = "detalles", target = "detalles"),
            @Mapping(source = "total", target = "total"),
            @Mapping(source = "totalCosto", target = "totalCosto"),
            @Mapping(expression = "java(entity.getTotalDescuentos())", target = "totalDescuentos"),

            @Mapping(source = "tipoEnvio", target = "tipoEnvio"),
            @Mapping(source = "domicilio", target = "domicilio"),

            @Mapping(source = "formaPago", target = "formaPago"),
            @Mapping(source = "pagoConfirmado", target = "pagoConfirmado"),
            @Mapping(source = "fechaConfirmacionPago", target = "fechaConfirmacionPago"),
            @Mapping(expression = "java(getNombreUsuario(entity.getUsuarioConfirmaPago()))", target = "nombreCajeroConfirmaPago"),
            @Mapping(source = "codigoMercadoPago", target = "codigoMercadoPago"),

            @Mapping(source = "horaEstimadaFinalizacion", target = "horaEstimadaFinalizacion"),
            @Mapping(source = "tiempoExtensionMinutos", target = "tiempoExtensionMinutos"),
            @Mapping(expression = "java(entity.estaRetrasado())", target = "estaRetrasado"),

            @Mapping(source = "fechaInicioPreparacion", target = "fechaInicioPreparacion"),
            @Mapping(source = "fechaListo", target = "fechaListo"),
            @Mapping(source = "fechaEntregado", target = "fechaEntregado"),
            @Mapping(source = "fechaCancelado", target = "fechaCancelado"),
            @Mapping(source = "motivoCancelacion", target = "motivoCancelacion"),
            @Mapping(expression = "java(getNombreUsuario(entity.getUsuarioCancela()))", target = "nombreUsuarioCancela"),

            @Mapping(source = "usuarioDelivery.idUsuario", target = "idUsuarioDelivery"),
            @Mapping(expression = "java(getNombreUsuario(entity.getUsuarioDelivery()))", target = "nombreDelivery"),

            @Mapping(source = "observaciones", target = "observaciones")
    })
    PedidoResponse toAdminResponse(Pedido entity);

    // ========== TO RESPONSE - CAJERO ==========

    @Mappings({
            @Mapping(source = "idPedido", target = "idPedido"),
            @Mapping(source = "fecha", target = "fecha"),
            @Mapping(source = "estado", target = "estado"),

            @Mapping(expression = "java(getNombreCompletoCliente(entity))", target = "nombreCliente"),
            @Mapping(source = "cliente.telefono", target = "telefonoCliente"),

            @Mapping(source = "detalles", target = "detalles"),
            @Mapping(source = "total", target = "total"),
            @Mapping(expression = "java(entity.getTotalDescuentos())", target = "totalDescuentos"),

            @Mapping(source = "tipoEnvio", target = "tipoEnvio"),
            @Mapping(source = "domicilio", target = "domicilio"),

            @Mapping(source = "formaPago", target = "formaPago"),
            @Mapping(source = "pagoConfirmado", target = "pagoConfirmado"),

            @Mapping(source = "horaEstimadaFinalizacion", target = "horaEstimadaFinalizacion"),

            @Mapping(source = "usuarioDelivery.idUsuario", target = "idUsuarioDelivery"),
            @Mapping(expression = "java(getNombreUsuario(entity.getUsuarioDelivery()))", target = "nombreDelivery"),

            @Mapping(source = "observaciones", target = "observaciones")
    })
    PedidoCajeroResponse toCajeroResponse(Pedido entity);

    // ========== TO RESPONSE - COCINERO ==========

    @Mappings({
            @Mapping(source = "idPedido", target = "idPedido"),
            @Mapping(source = "fecha", target = "fecha"),
            @Mapping(source = "estado", target = "estado"),

            @Mapping(source = "detalles", target = "detalles"),

            @Mapping(source = "horaEstimadaFinalizacion", target = "horaEstimadaFinalizacion"),
            @Mapping(source = "tiempoExtensionMinutos", target = "tiempoExtensionMinutos"),
            @Mapping(expression = "java(entity.estaRetrasado())", target = "estaRetrasado"),

            @Mapping(source = "observaciones", target = "observaciones")
    })
    PedidoCocineroResponse toCocineroResponse(Pedido entity);

    // ========== TO RESPONSE - DELIVERY ==========

    @Mappings({
            @Mapping(source = "idPedido", target = "idPedido"),
            @Mapping(source = "fecha", target = "fecha"),
            @Mapping(source = "estado", target = "estado"),

            @Mapping(expression = "java(getNombreCompletoCliente(entity))", target = "nombreCliente"),
            @Mapping(source = "cliente.telefono", target = "telefonoCliente"),

            @Mapping(source = "domicilio", target = "domicilio"),

            @Mapping(source = "detalles", target = "detalles"),
            @Mapping(source = "total", target = "total"),

            @Mapping(source = "observaciones", target = "observaciones")
    })
    PedidoDeliveryResponse toDeliveryResponse(Pedido entity);

    // ========== TO RESPONSE - CLIENTE ==========

    @Mappings({
            @Mapping(source = "idPedido", target = "idPedido"),
            @Mapping(source = "fecha", target = "fecha"),
            @Mapping(source = "estado", target = "estado"),

            @Mapping(source = "detalles", target = "detalles"),
            @Mapping(source = "total", target = "total"),
            @Mapping(expression = "java(entity.getTotalDescuentos())", target = "totalDescuentos"),

            @Mapping(source = "tipoEnvio", target = "tipoEnvio"),
            @Mapping(source = "formaPago", target = "formaPago"),
            @Mapping(source = "pagoConfirmado", target = "pagoConfirmado"),

            @Mapping(source = "horaEstimadaFinalizacion", target = "horaEstimadaFinalizacion"),

            @Mapping(expression = "java(getNombreUsuario(entity.getUsuarioDelivery()))", target = "nombreDelivery"),

            @Mapping(source = "observaciones", target = "observaciones"),
            @Mapping(source = "motivoCancelacion", target = "motivoCancelacion")
    })
    PedidoClienteResponse toClienteResponse(Pedido entity);

    // ========== TO ENTITY ==========

    @Mappings({
            @Mapping(target = "idPedido", ignore = true),
            @Mapping(target = "fecha", expression = "java(java.time.LocalDateTime.now())"),
            @Mapping(target = "estado", constant = "PENDIENTE"),

            @Mapping(source = "tipoEnvio", target = "tipoEnvio"),
            @Mapping(source = "formaPago", target = "formaPago"),
            @Mapping(source = "observaciones", target = "observaciones"),
            @Mapping(source = "codigoMercadoPago", target = "codigoMercadoPago"),

            @Mapping(target = "total", constant = "0.0"),
            @Mapping(target = "totalCosto", constant = "0.0"),
            @Mapping(target = "pagoConfirmado", expression = "java(getPagoConfirmadoInicial(request.getFormaPago()))"),
            @Mapping(target = "tiempoExtensionMinutos", constant = "0"),

            @Mapping(target = "cliente", ignore = true),
            @Mapping(target = "domicilio", ignore = true),
            @Mapping(target = "detalles", ignore = true),
            @Mapping(target = "factura", ignore = true),

            @Mapping(target = "horaEstimadaFinalizacion", ignore = true),
            @Mapping(target = "fechaConfirmacionPago", ignore = true),
            @Mapping(target = "usuarioConfirmaPago", ignore = true),
            @Mapping(target = "fechaInicioPreparacion", ignore = true),
            @Mapping(target = "fechaListo", ignore = true),
            @Mapping(target = "fechaEntregado", ignore = true),
            @Mapping(target = "fechaCancelado", ignore = true),
            @Mapping(target = "motivoCancelacion", ignore = true),
            @Mapping(target = "usuarioCancela", ignore = true),
            @Mapping(target = "usuarioDelivery", ignore = true)
    })
    Pedido toEntity(CrearPedidoRequest request);

    // ========== MÉTODOS DEFAULT ==========

    /**
     * Obtiene el nombre completo del cliente desde Usuario
     */
    default String getNombreCompletoCliente(Pedido pedido) {
        if (pedido.getCliente() == null || pedido.getCliente().getUsuario() == null) {
            return null;
        }
        com.elbuensabor.entities.Usuario usuario = pedido.getCliente().getUsuario();
        return usuario.getNombre() + " " + usuario.getApellido();
    }

    /**
     * Obtiene el nombre completo de un usuario
     */
    default String getNombreUsuario(com.elbuensabor.entities.Usuario usuario) {
        if (usuario == null)
            return null;
        return usuario.getNombre() + " " + usuario.getApellido();
    }

    /**
     * Determina si el pago está confirmado inicialmente según forma de pago
     */
    default Boolean getPagoConfirmadoInicial(com.elbuensabor.entities.FormaPago formaPago) {
        return formaPago == com.elbuensabor.entities.FormaPago.MERCADO_PAGO;
    }
}