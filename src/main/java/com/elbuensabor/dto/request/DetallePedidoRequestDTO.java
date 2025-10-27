package com.elbuensabor.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetallePedidoRequestDTO {

    @NotNull(message = "El ID del artículo es obligatorio")
    private Long idArticulo;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    @Size(max = 200, message = "Las observaciones del producto no pueden exceder 200 caracteres")
    private String observaciones;

    // ✅ NUEVO: Promoción seleccionada por el cliente en el carrito
    private Long idPromocionSeleccionada;
}