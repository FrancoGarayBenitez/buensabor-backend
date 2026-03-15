package com.elbuensabor.dto.response.pedido;

import com.elbuensabor.dto.response.DomicilioResponseDTO;
import com.elbuensabor.entities.Estado;
import com.elbuensabor.entities.FormaPago;
import com.elbuensabor.entities.TipoEnvio;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponse {

    // Identificación
    private Long idPedido;
    private LocalDateTime fecha;
    private Estado estado;

    // Cliente
    private Long idCliente;
    private String nombreCliente;
    private String emailCliente;
    private String telefonoCliente;

    // Detalles del pedido
    private List<DetallePedidoResponse> detalles;
    private Double total;
    private Double totalCosto; // Solo para admin
    private Double totalDescuentos;

    // Tipo de pedido
    private TipoEnvio tipoEnvio;
    private DomicilioResponseDTO domicilio; // null si es RETIRO

    // Pago
    private FormaPago formaPago;
    private Boolean pagoConfirmado;
    private LocalDateTime fechaConfirmacionPago;
    private String nombreCajeroConfirmaPago;
    private String codigoMercadoPago;

    // Tiempos
    private LocalTime horaEstimadaFinalizacion;
    private Integer tiempoExtensionMinutos;
    private Boolean estaRetrasado;

    // Auditoría
    private LocalDateTime fechaInicioPreparacion;
    private LocalDateTime fechaListo;
    private LocalDateTime fechaEntregado;
    private LocalDateTime fechaCancelado;
    private String motivoCancelacion;
    private String nombreUsuarioCancela;

    // Delivery
    private Long idUsuarioDelivery;
    private String nombreDelivery;

    // Observaciones
    private String observaciones;
}