package com.elbuensabor.dto.response.pedido;

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
public class PedidoClienteResponse {

    private Long idPedido;
    private LocalDateTime fecha;
    private Estado estado;

    private List<DetallePedidoResponse> detalles;
    private Double total;
    private Double totalDescuentos;

    private TipoEnvio tipoEnvio;
    private FormaPago formaPago;
    private Boolean pagoConfirmado;

    private LocalTime horaEstimadaFinalizacion;

    private String nombreDelivery; // Si está asignado

    private String observaciones;
    private String motivoCancelacion; // Si fue cancelado
}