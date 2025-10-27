package com.elbuensabor.dto.response;

import com.elbuensabor.dto.request.ImagenDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticuloInsumoResponseDTO {

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

    // Campos específicos de ArticuloInsumo
    private Double precioCompra;
    private Integer stockActual;
    private Integer stockMaximo;
    private Boolean esParaElaborar;

    // Información calculada
    private Double porcentajeStock; // (stockActual / stockMaximo) * 100
    private String estadoStock; // "CRITICO", "BAJO", "NORMAL", "ALTO"
    private Integer stockDisponible; // stockActual (para uso externo)

    // Imagen
    private List<ImagenDTO> imagenes;

    // Información de uso (cuántos productos manufacturados lo usan)
    private Integer cantidadProductosQueLoUsan;
}