package com.elbuensabor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleManufacturadoResponseDTO {
    private Long idDetalleManufacturado;
    private Double cantidad;

    // Información del Insumo
    private Long idArticuloInsumo;
    private String denominacionInsumo;
    private String unidadMedidaInsumo;
    private Double costoInsumo; // precioCompra del insumo
}
