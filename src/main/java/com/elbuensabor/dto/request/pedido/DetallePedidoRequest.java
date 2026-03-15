package com.elbuensabor.dto.request.pedido;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetallePedidoRequest {

    private Long idArticulo;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    private String observaciones; // Ej: "sin cebolla", "bien cocido"

    private Long idPromocion; // Si aplica una promoción
}