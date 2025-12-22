package com.elbuensabor.dto.response;

import com.elbuensabor.dto.request.ImagenDTO;
import com.elbuensabor.entities.TipoCategoria;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticuloManufacturadoResponseDTO {
    // Campos heredados de Articulo
    private Long idArticulo;
    private String denominacion;
    private Double precioVenta;
    private Boolean eliminado;

    // Unidad de medida
    private Long idUnidadMedida;
    private String denominacionUnidadMedida;

    // Categoría
    private Long idCategoria;
    private String denominacionCategoria;
    private Boolean esSubcategoria;
    private String denominacionCategoriaPadre;
    private TipoCategoria tipoCategoria;

    // Manufacturado
    private String descripcion;
    private String preparacion;
    private Integer tiempoEstimadoEnMinutos;

    // Receta
    private List<DetalleManufacturadoResponseDTO> detalles;

    // Imágenes
    private List<ImagenDTO> imagenes;

    // --- CAMPOS CALCULADOS Y DE NEGOCIO (REFACTORIZADOS) ---

    // Costos y Márgenes
    private Double costoProduccion; // suma de subtotales de ingredientes
    private Double margenGanancia; // multiplicador, ej: 1.3
    private Double margenGananciaPorcentaje; // porcentaje, ej: 30.0

    // Stock
    private Boolean stockSuficiente; // true si se puede preparar al menos 1
    private Integer cantidadMaximaPreparable; // cuántos se pueden preparar con el stock actual
}
