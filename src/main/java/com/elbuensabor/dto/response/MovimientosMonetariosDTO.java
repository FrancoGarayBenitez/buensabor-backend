package com.elbuensabor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor // <-- ESTA ANOTACIÓN ES LA CLAVE
public class MovimientosMonetariosDTO {
    private Double ingresos;
    private Double costos;
    private Double ganancias;
}
