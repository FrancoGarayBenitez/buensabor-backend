package com.elbuensabor.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacturaRequestDTO {

    @NotNull(message = "El ID del pedido es obligatorio")
    private Long pedidoId;

    @PositiveOrZero(message = "El descuento no puede ser negativo")
    private Double descuento = 0.0;

    @PositiveOrZero(message = "Los gastos de envío no pueden ser negativos")
    private Double gastosEnvio = 0.0;
}