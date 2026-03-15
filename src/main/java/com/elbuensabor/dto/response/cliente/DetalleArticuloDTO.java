package com.elbuensabor.dto.response.cliente;

import com.elbuensabor.dto.request.ImagenDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO detallado de un artículo para el cliente.
 * Incluye toda la información necesaria para la decisión de compra.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleArticuloDTO {
    // Identificación
    private Long idArticulo;
    private String denominacion;
    private String descripcion;

    // Valores: "MANUFACTURADO" | "INSUMO"
    private String tipoArticulo;

    // Precios
    private Double precioOriginal;
    private Double precioFinal;
    private Integer porcentajeDescuento;
    private Double ahorroEnPesos; // Ej: "Ahorrás $500"

    // Disponibilidad
    private Boolean disponible;
    private String mensajeDisponibilidad; // "Disponible" / "Agotado" / "Últimas unidades"
    private Integer tiempoEstimadoEnMinutos;

    // Promoción detallada (solo MANUFACTURADO)
    private PromocionClienteDTO promocionActiva;

    // Categoría
    private Long idCategoria;
    private String nombreCategoria;

    // Ingredientes principales (solo MANUFACTURADO)
    private List<String> ingredientesPrincipales;

    // Imágenes
    private List<ImagenDTO> imagenes;

    // ✅ NUEVO: campos exclusivos de INSUMO (bebidas)
    private String unidadMedida;
    private Double stockActual;
}