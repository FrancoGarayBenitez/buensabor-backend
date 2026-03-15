package com.elbuensabor.dto.request.pedido;

import com.elbuensabor.entities.Estado;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambiarEstadoPedidoRequest {

    @NotNull(message = "El ID del pedido es obligatorio")
    private Long idPedido;

    @NotNull(message = "El nuevo estado es obligatorio")
    private Estado nuevoEstado;

    // El usuario que realiza el cambio se obtiene del JWT
}