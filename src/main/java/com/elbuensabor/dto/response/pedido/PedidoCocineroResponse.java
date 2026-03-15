package com.elbuensabor.dto.response.pedido;

import com.elbuensabor.entities.Estado;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoCocineroResponse {

    private Long idPedido;
    private LocalDateTime fecha;
    private Estado estado;

    private List<DetallePedidoResponse> detalles; // Solo nombre, cantidad y observaciones

    private LocalTime horaEstimadaFinalizacion;
    private Integer tiempoExtensionMinutos;
    private Boolean estaRetrasado;

    private String observaciones;
}