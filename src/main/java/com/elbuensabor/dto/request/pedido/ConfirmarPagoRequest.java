package com.elbuensabor.dto.request.pedido;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmarPagoRequest {

    @NotNull(message = "El ID del pedido es obligatorio")
    private Long idPedido;

    // El ID del cajero se obtiene del token JWT
}