package com.elbuensabor.dto.request;

import com.elbuensabor.entities.TipoDescuento;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromocionRequestDTO {

    @NotBlank(message = "La denominación es obligatoria")
    @Size(max = 100, message = "La denominación no puede exceder 100 caracteres")
    private String denominacion;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime fechaDesde;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDateTime fechaHasta;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaDesde;

    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime horaHasta;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcionDescuento;

    @NotNull(message = "El tipo de descuento es obligatorio")
    private TipoDescuento tipoDescuento;

    @NotNull(message = "El valor del descuento es obligatorio")
    @DecimalMin(value = "0.0", message = "El valor del descuento no puede ser negativo")
    private Double valorDescuento;

    // ✅ NUEVO: Campo agregado para el precio promocional
    @NotNull(message = "El precio promocional es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio promocional no puede ser negativo")
    private Double precioPromocional;

    @DecimalMin(value = "1", message = "La cantidad mínima debe ser al menos 1")
    private Integer cantidadMinima = 1;

    private Boolean activo = true;

    @NotEmpty(message = "Debe especificar al menos un artículo")
    private List<Long> idsArticulos;

    // Para futuras implementaciones con imágenes
    private List<String> urlsImagenes;
}