package com.elbuensabor.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticuloManufacturadoRequestDTO {

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

    // Campos específicos de ArticuloManufacturado
    @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
    private String descripcion;

    @NotNull(message = "El tiempo estimado en minutos es obligatorio")
    @Min(value = 0, message = "El tiempo estimado no puede ser negativo")
    private Integer tiempoEstimadoEnMinutos;

    @NotBlank(message = "La preparación es obligatoria")
    private String preparacion;

    @NotNull(message = "El margen de ganancia es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El margen de ganancia debe ser mayor o igual a 0")
    private Double margenGanancia;

    // Detalles de la receta
    @NotEmpty(message = "El producto debe tener al menos un ingrediente")
    private List<@Valid DetalleManufacturadoRequestDTO> detalles;

    // Imágenes
    private List<ImagenDTO> imagenes;
}