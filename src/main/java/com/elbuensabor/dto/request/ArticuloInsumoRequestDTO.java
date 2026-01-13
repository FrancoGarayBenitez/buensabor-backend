package com.elbuensabor.dto.request;

import java.util.List;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticuloInsumoRequestDTO {

    // ==================== CAMPOS HEREDADOS DE ARTICULO ====================
    @NotBlank(message = "La denominación es obligatoria")
    private String denominacion;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio de venta debe ser mayor o igual a 0")
    private Double precioVenta;

    @NotNull(message = "La unidad de medida es obligatoria")
    private Long idUnidadMedida;

    @NotNull(message = "La categoría es obligatoria")
    private Long idCategoria;

    private List<ImagenDTO> imagenes;

    // ==================== CAMPOS ESPECÍFICOS DE ARTICULOINSUMO
    // ====================
    @NotNull(message = "El precio de compra es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio de compra debe ser mayor o igual a 0")
    private Double precioCompra;

    @NotNull(message = "El stock actual es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El stock actual no puede ser negativo")
    private Double stockActual;

    @NotNull(message = "El stock máximo es obligatorio")
    @DecimalMin(value = "0.1", inclusive = false, message = "El stock máximo debe ser mayor a 0")
    private Double stockMaximo;

    @NotNull(message = "Debe especificar si es para elaborar")
    private Boolean esParaElaborar;

    // ==================== VALIDACIÓN CRUZADA ====================
    @AssertTrue(message = "El stock actual no puede superar el stock máximo")
    private boolean isStockValid() {
        if (stockActual == null || stockMaximo == null) {
            return true;
        }
        return stockActual <= stockMaximo;
    }
}