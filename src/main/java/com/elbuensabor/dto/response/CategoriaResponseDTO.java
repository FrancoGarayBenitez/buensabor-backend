package com.elbuensabor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaResponseDTO {
    private Long idCategoria;
    private String denominacion;
    private Boolean esSubcategoria;

    // Información de la categoría padre (si es subcategoría)
    private Long idCategoriaPadre;
    private String denominacionCategoriaPadre;

    // Lista de subcategorías (si es categoría padre)
    private List<CategoriaSimpleDTO> subcategorias;

    // Cantidad de artículos en esta categoría
    private Integer cantidadArticulos;
}

