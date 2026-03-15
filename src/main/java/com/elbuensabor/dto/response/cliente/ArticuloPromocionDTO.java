package com.elbuensabor.dto.response.cliente;

import com.elbuensabor.dto.request.ImagenDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO completo para mostrar artículos incluidos en una promoción combo.
 * ✅ Incluye información necesaria para el detalle visual.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticuloPromocionDTO {
    // Identificación
    private Long idArticulo;
    private String denominacion;
    private String descripcion; // ✅ NUEVO - Para mostrar en detalle

    // Cantidad y precios
    private Integer cantidad; // Cuántas unidades incluye el combo
    private Double precioOriginal; // Para mostrar el ahorro

    // ✅ NUEVOS CAMPOS - Información adicional
    private Integer tiempoEstimadoEnMinutos;
    private String nombreCategoria;

    // ✅ NUEVO - Imagen
    private ImagenDTO imagenPrincipal;
}