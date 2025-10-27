package com.elbuensabor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO simple para evitar referencias circulares en subcategorías
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaSimpleDTO {
    private Long idCategoria;
    private String denominacion;
    private Integer cantidadArticulos;
}
