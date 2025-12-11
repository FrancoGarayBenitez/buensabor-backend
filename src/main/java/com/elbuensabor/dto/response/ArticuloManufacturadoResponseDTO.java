package com.elbuensabor.dto.response;

import com.elbuensabor.dto.request.ImagenDTO;
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

    // Información de Unidad de Medida
    private Long idUnidadMedida;
    private String denominacionUnidadMedida;

    // Información de Categoría
    private Long idCategoria;
    private String denominacionCategoria;
    private Boolean esSubcategoria;
    private String denominacionCategoriaPadre;

    // Campos específicos de ArticuloManufacturado
    private String descripcion;
    private Integer tiempoEstimadoEnMinutos;
    private String preparacion;
    private Double margenGanancia;
    private Double costoProduccion;

    // Detalles de la receta
    private List<DetalleManufacturadoResponseDTO> detalles;

    // Imágenes
    private List<ImagenDTO> imagenes;

    // Información calculada
    private Boolean stockSuficiente; // Indica si hay stock de todos los ingredientes
    private Integer cantidadMaximaPreparable; // Cantidad máxima que se puede producir con el stock actual
}
