package com.elbuensabor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RankingProductoDTO {
    private String denominacionProducto;
    // Ojo: SUM(cantidad) puede ser Long. Ajusta el tipo si es necesario.
    private Long cantidadVendida;
    private Double totalVendido;
}
