package com.elbuensabor.dto.request.pedido;

import com.elbuensabor.entities.FormaPago;
import com.elbuensabor.entities.TipoEnvio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearPedidoRequest {

    @NotNull(message = "El tipo de envío es obligatorio")
    private TipoEnvio tipoEnvio;

    @NotNull(message = "La forma de pago es obligatoria")
    private FormaPago formaPago;

    private Long idDomicilio; // Obligatorio si tipoEnvio = DELIVERY

    @NotEmpty(message = "El pedido debe contener al menos un producto")
    @Valid
    private List<DetallePedidoRequest> detalles;

    private String observaciones; // Observaciones generales del cliente

    // Para MercadoPago
    private String codigoMercadoPago; // Si ya hizo el pago, se envía el ID
}