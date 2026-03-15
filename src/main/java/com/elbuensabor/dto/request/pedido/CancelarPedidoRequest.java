package com.elbuensabor.dto.request.pedido;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelarPedidoRequest {

    @NotNull(message = "El ID del pedido es obligatorio")
    private Long idPedido;

    @NotBlank(message = "Debe especificar un motivo de cancelación")
    private String motivo;

    // El usuario que cancela se obtiene del JWT
}