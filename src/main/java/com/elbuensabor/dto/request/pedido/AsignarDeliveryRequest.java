package com.elbuensabor.dto.request.pedido;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignarDeliveryRequest {

    @NotNull(message = "El ID del pedido es obligatorio")
    private Long idPedido;

    @NotNull(message = "El ID del delivery es obligatorio")
    private Long idUsuarioDelivery; // Usuario con rol DELIVERY
}