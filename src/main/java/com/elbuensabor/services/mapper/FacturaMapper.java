package com.elbuensabor.services.mapper;

import com.elbuensabor.dto.request.FacturaRequestDTO;
import com.elbuensabor.dto.response.FacturaResponseDTO;
import com.elbuensabor.dto.response.PagoSummaryDTO;
import com.elbuensabor.entities.Factura;
import com.elbuensabor.entities.Pago;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface FacturaMapper extends BaseMapper<Factura, FacturaResponseDTO> {

    // ==================== ENTITY → RESPONSE DTO ====================
    @Override
    @Mapping(source = "pedido.idPedido", target = "pedidoId")
    @Mapping(source = "pedido.estado", target = "estadoPedido")
    @Mapping(source = "pedido.tipoEnvio", target = "tipoEnvio")
    @Mapping(source = "pedido.cliente.idCliente", target = "clienteId")
    @Mapping(source = "pedido.cliente.usuario.nombre", target = "nombreCliente")
    @Mapping(source = "pedido.cliente.usuario.apellido", target = "apellidoCliente")
    @Mapping(target = "pagos", ignore = true) // Se mapea manualmente en el service
    @Mapping(target = "totalPagado", ignore = true) // Se calcula en el service
    @Mapping(target = "saldoPendiente", ignore = true) // Se calcula en el service
    @Mapping(target = "completamentePagada", ignore = true) // Se calcula en el service
    FacturaResponseDTO toDTO(Factura entity);

    // ==================== REQUEST DTO → ENTITY ====================
    @Mapping(target = "idFactura", ignore = true)
    @Mapping(target = "fechaFactura", ignore = true) // Se asigna en el service
    @Mapping(target = "nroComprobante", ignore = true) // Se genera en el service
    @Mapping(target = "subTotal", ignore = true) // Se calcula en el service
    @Mapping(target = "totalVenta", ignore = true) // Se calcula en el service
    @Mapping(target = "pedido", ignore = true) // Se asigna en el service
    @Mapping(target = "pagos", ignore = true)
    Factura toEntity(FacturaRequestDTO dto);

    // ==================== RESPONSE DTO → ENTITY (GENERIC) ====================
    @Override
    @Mapping(target = "pedido", ignore = true)
    @Mapping(target = "pagos", ignore = true)
    Factura toEntity(FacturaResponseDTO dto);

    // ==================== UPDATE FROM DTO ====================
    @Override
    @Mapping(target = "idFactura", ignore = true)
    @Mapping(target = "fechaFactura", ignore = true)
    @Mapping(target = "nroComprobante", ignore = true)
    @Mapping(target = "pedido", ignore = true)
    @Mapping(target = "pagos", ignore = true)
    void updateEntityFromDTO(FacturaResponseDTO dto, @MappingTarget Factura entity);

    // ==================== MÉTODOS AUXILIARES ====================
    default PagoSummaryDTO mapPagoSummary(Pago pago) {
        if (pago == null) return null;

        return new PagoSummaryDTO(
                pago.getIdPago(),
                pago.getFormaPago().name(),
                pago.getEstado().name(),
                pago.getMonto(),
                pago.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );
    }
}