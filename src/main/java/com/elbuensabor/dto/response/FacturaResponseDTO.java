package com.elbuensabor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

import com.elbuensabor.dto.response.pedido.DetallePedidoResponse;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacturaResponseDTO {

    private Long idFactura;
    private LocalDate fechaFactura;
    private String nroComprobante;
    private Double subTotal;
    private Double descuento;
    private Double gastosEnvio;
    private Double totalVenta;

    // Información del pedido asociado
    private Long pedidoId;
    private String estadoPedido;
    private String tipoEnvio;

    // 🆕 INFORMACIÓN COMPLETA DEL CLIENTE
    private Long clienteId;
    private String nombreCliente;
    private String apellidoCliente;
    private String emailCliente; // ✅ NUEVO
    private String telefonoCliente; // ✅ NUEVO

    // 🆕 DOMICILIO DE ENTREGA (si es DELIVERY)
    private DomicilioResponseDTO domicilioEntrega; // ✅ NUEVO

    // 🆕 DETALLES REALES DEL PEDIDO
    private List<DetallePedidoResponse> detallesPedido; // ✅ NUEVO

    // 🆕 OBSERVACIONES DEL PEDIDO
    private String observacionesPedido; // ✅ NUEVO

    // Información de pagos (existente)
    private List<PagoSummaryDTO> pagos;
    private Double totalPagado;
    private Double saldoPendiente;
    private Boolean completamentePagada;
}