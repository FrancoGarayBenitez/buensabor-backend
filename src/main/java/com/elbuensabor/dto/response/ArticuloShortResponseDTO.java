package com.elbuensabor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticuloShortResponseDTO {
    private Long id;
    private String denominacion;
    private Double precioVenta;
}
