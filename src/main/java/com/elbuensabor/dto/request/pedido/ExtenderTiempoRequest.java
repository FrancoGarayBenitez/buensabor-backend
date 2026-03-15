package com.elbuensabor.dto.request.pedido;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtenderTiempoRequest {

    @NotNull(message = "El ID del pedido es obligatorio")
    private Long idPedido;

    @NotNull(message = "Los minutos de extensión son obligatorios")
    @Min(value = 5, message = "La extensión debe ser de al menos 5 minutos")
    private Integer minutosExtension; // Ej: 10, 15, 20
}