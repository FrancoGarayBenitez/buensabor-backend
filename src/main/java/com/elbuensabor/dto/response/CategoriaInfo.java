package com.elbuensabor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO anidado para información de categoría (más claro)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaInfo {
    private Long idCategoria;
    private String denominacion;           // ej: "Pizzas"
    private Boolean esSubcategoria;        // ej: false
    private String categoriaPadre;         // ej: null (porque Pizzas es principal)
}
