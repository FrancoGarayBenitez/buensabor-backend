package com.elbuensabor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoPrecioDTO {
    private Long idHistoricoPrecio;
    private Long idArticulo;
    private Double precioUnitario;
    private LocalDateTime fecha;
    private Double cantidad;
}
