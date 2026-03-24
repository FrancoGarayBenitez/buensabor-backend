package com.elbuensabor.dto.response.cliente;

import com.elbuensabor.dto.request.ImagenDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para mostrar artículos en el catálogo del cliente.
 * Incluye información de promociones aplicables.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogoArticuloDTO {
    // Identificación
    private Long idArticulo;
    private String denominacion;
    private String descripcion;

    // Precios
    private Double precioOriginal;
    private Double precioFinal; // Con descuento aplicado (si tiene)
    private Integer porcentajeDescuento; // Para mostrar badge "20% OFF"

    // Disponibilidad
    private Boolean disponible;
    private Integer tiempoEstimadoEnMinutos;

    // Promoción (si aplica)
    private Boolean tienePromocion;
    private Long idPromocion; // Para navegar al detalle de la promo
    private String etiquetaPromocion; // Ej: "HAPPY HOUR", "PROMO 2x1", "20% OFF"

    // Categoría
    private Long idCategoria;
    private String nombreCategoria;

    // Imagen principal (solo la primera)
    private ImagenDTO imagenPrincipal;

    // Tipo de artículo
    private String tipoArticulo; // "MANUFACTURADO" o "INSUMO"
}
