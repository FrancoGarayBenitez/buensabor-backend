package com.elbuensabor.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromocionDetalleDTO {
    private int cantidad;
    private Long idArticulo;
}