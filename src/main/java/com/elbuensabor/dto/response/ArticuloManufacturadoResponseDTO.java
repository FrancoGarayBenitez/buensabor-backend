package com.elbuensabor.dto.response;

import com.elbuensabor.dto.request.ImagenDTO;
import com.elbuensabor.dto.request.ManufacturadoDetalleDTO;
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


    // Información de Unidad de Medida (cómo se VENDE el producto)
    private Long idUnidadMedida;
    private String denominacionUnidadMedida; // ej: "Unidades"

    // Información de Categoría (agrupada para mayor claridad)
    private CategoriaInfo categoria;

    // Campos específicos de ArticuloManufacturado
    private String descripcion;
    private Integer tiempoEstimadoEnMinutos;
    private String preparacion;

    // Lista de ingredientes con detalles completos
    private List<ManufacturadoDetalleDTO> detalles;

    // Información calculada
    private Double costoTotal; // Suma de costos de ingredientes
    private Double margenGanancia; // precioVenta / costoTotal
    private Integer cantidadIngredientes; // detalles.size()
    private Boolean stockSuficiente; // Si hay stock para preparar
    private Integer cantidadMaximaPreparable; // Según stock disponible

    // Imagen
    private List<ImagenDTO> imagenes;

    // Información de uso
    private Integer cantidadVendida; // Histórico de ventas
}

