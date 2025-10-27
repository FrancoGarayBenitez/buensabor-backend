package com.elbuensabor.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromocionAplicacionDTO {

    @NotNull(message = "El ID de la promoción es obligatorio")
    private Long idPromocion;

    @NotNull(message = "El ID del artículo es obligatorio")
    private Long idArticulo;

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    private Double precioUnitario;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "1", message = "La cantidad debe ser al menos 1")
    private Integer cantidad;
}
