package com.elbuensabor.dto.response.pedido;

import com.elbuensabor.dto.response.DomicilioResponseDTO;
import com.elbuensabor.entities.Estado;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDeliveryResponse {

    private Long idPedido;
    private LocalDateTime fecha;
    private Estado estado;

    private String nombreCliente;
    private String telefonoCliente;

    private DomicilioResponseDTO domicilio;

    private List<DetallePedidoResponse> detalles; // Solo para verificar
    private Double total;

    private String observaciones;
}