package com.elbuensabor.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManufacturadoDetalleDTO {

    // Para updates, puede incluir ID del detalle
    private Long idDetalleManufacturado;

    @NotNull(message = "El artículo insumo es obligatorio")
    private Long idArticuloInsumo;

    // Información adicional del insumo (solo para response)
    private String denominacionInsumo;
    private String unidadMedida;
    private Double precioCompraUnitario;
    private Double subtotal; // cantidad * precioCompra

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.0", inclusive = false, message = "La cantidad debe ser mayor a 0")
    private Double cantidad;
}