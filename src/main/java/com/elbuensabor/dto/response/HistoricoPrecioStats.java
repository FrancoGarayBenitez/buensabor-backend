package com.elbuensabor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoPrecioStats {
    private Integer totalRegistros;
    private Double precioPromedio;
    private Double precioMinimo;
    private Double precioMaximo;
}