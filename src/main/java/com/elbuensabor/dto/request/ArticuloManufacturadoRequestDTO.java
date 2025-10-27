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

    @NotNull(message = "La unidad de medida es obligatoria")
    private Long idUnidadMedida;

    @NotNull(message = "La categoría es obligatoria")
    private Long idCategoria;

    // Campos específicos de ArticuloManufacturado
    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String descripcion;

    @NotNull(message = "El tiempo estimado es obligatorio")
    @Min(value = 1, message = "El tiempo estimado debe ser mayor a 0")
    private Integer tiempoEstimadoEnMinutos;

    private String preparacion;

    // Precio de venta (opcional si se calcula automáticamente)
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio de venta debe ser mayor a 0")
    private Double precioVenta; // Puede ser null si se calcula automáticamente

    // Margen de ganancia (para calcular precio automáticamente)
    @DecimalMin(value = "1.0", message = "El margen debe ser mayor a 1")
    private Double margenGanancia; // Ej: 2.5 = 250% sobre el costo

    // Lista de ingredientes/insumos
    @Valid
    @NotEmpty(message = "Debe tener al menos un ingrediente")
    private List<ManufacturadoDetalleDTO> detalles;

    // Imagen opcional
    private ImagenDTO imagen;
}