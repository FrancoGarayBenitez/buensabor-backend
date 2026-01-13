package com.elbuensabor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromocionDetalleResponseDTO {
    private int cantidad;
    private ArticuloShortResponseDTO articulo;
}