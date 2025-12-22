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

    // Artículo base
    @NotBlank
    private String denominacion;

    // El precio de venta ahora es opcional en el request, se puede calcular desde
    // el costo y margen
    @DecimalMin(value = "0.0", inclusive = true)
    private Double precioVenta;

    @NotNull
    private Long idUnidadMedida;

    @NotNull
    private Long idCategoria; // debe ser de tipo COMIDAS

    // Manufacturado
    @Size(max = 1000)
    private String descripcion;

    @Size(max = 4000)
    private String preparacion;

    @NotNull
    @Min(0)
    private Integer tiempoEstimadoEnMinutos;

    // ✅ Margen como porcentaje (0–100), más intuitivo para el usuario
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private Double margenGananciaPorcentaje;

    // Receta
    @NotNull
    @Size(min = 1, message = "Debe contener al menos un ingrediente")
    @Valid
    private List<DetalleManufacturadoRequestDTO> detalles;

    // Imágenes
    private List<ImagenDTO> imagenes;
}