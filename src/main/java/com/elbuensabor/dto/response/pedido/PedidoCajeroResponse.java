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
public class PedidoCajeroResponse {

    private Long idPedido;
    private LocalDateTime fecha;
    private Estado estado;

    private String nombreCliente;
    private String telefonoCliente;

    private List<DetallePedidoResponse> detalles;
    private Double total;
    private Double totalDescuentos;

    private TipoEnvio tipoEnvio;
    private DomicilioResponseDTO domicilio;

    private FormaPago formaPago;
    private Boolean pagoConfirmado;

    private LocalTime horaEstimadaFinalizacion;

    private Long idUsuarioDelivery;
    private String nombreDelivery;

    private String observaciones;
}