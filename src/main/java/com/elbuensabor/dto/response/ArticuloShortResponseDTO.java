package com.elbuensabor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticuloShortResponseDTO {
    private Long id;
    private String denominacion;
    private Double precioVenta;

    // ✅ NUEVO: costo según tipo de artículo
    // ArticuloManufacturado → costoProduccion
    // ArticuloInsumo → precioCompra
    private Double costo;
    private String tipoArticulo; // "MANUFACTURADO" | "INSUMO"
}
