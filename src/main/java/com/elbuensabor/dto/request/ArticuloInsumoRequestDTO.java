package com.elbuensabor.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticuloInsumoRequestDTO {

    // Campos heredados de Articulo
    @NotBlank(message = "La denominación es obligatoria")
    private String denominacion;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio de venta debe ser mayor a 0")
    private Double precioVenta;

    @NotNull(message = "La unidad de medida es obligatoria")
    private Long idUnidadMedida;

    @NotNull(message = "La categoría es obligatoria")
    private Long idCategoria;

    // Campos específicos de ArticuloInsumo
    @NotNull(message = "El precio de compra es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio de compra debe ser mayor a 0")
    private Double precioCompra;

    @NotNull(message = "El stock actual es obligatorio")
    @Min(value = 0, message = "El stock actual no puede ser negativo")
    private Integer stockActual;

    @NotNull(message = "El stock máximo es obligatorio")
    @Min(value = 1, message = "El stock máximo debe ser mayor a 0")
    private Integer stockMaximo;

    @NotNull(message = "Debe especificar si es para elaborar")
    private Boolean esParaElaborar;

    // Imagen opcional
    private ImagenDTO imagen;
}