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

    // ==================== INFORMACIÓN BÁSICA ====================
    private Long idArticulo;
    private String denominacion;
    private Double precioVenta;
    private Boolean eliminado;

    // ==================== UNIDAD DE MEDIDA ====================
    private Long idUnidadMedida;
    private String denominacionUnidadMedida;

    // ==================== CATEGORÍA ====================
    private Long idCategoria;
    private String denominacionCategoria;
    private Boolean esSubcategoria;
    private String denominacionCategoriaPadre;

    // ==================== DATOS DE INSUMO ====================
    private Double precioCompra;
    private Double stockActual;
    private Double stockMaximo;
    private Boolean esParaElaborar;

    // ==================== INFORMACIÓN CALCULADA (Calculada en Service)
    // ====================
    private String estadoStock; // CRITICO, BAJO, NORMAL, ALTO
    private Double porcentajeStock; // (stockActual / stockMaximo) * 100
    private Double stockDisponible; // Alias de stockActual
    private Double costoTotalInventario; // precioCompra * stockActual
    private Double margenGanancia; // ((precioVenta - precioCompra) / precioCompra) * 100

    // ==================== RELACIONES ====================
    private List<ImagenDTO> imagenes;
    private Integer cantidadProductosQueLoUsan;
}