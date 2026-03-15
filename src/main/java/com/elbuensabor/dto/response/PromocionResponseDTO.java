package com.elbuensabor.dto.response;

import com.elbuensabor.dto.request.ImagenDTO;
import com.elbuensabor.entities.EstadoPromocion;
import com.elbuensabor.entities.TipoDescuento;
import com.elbuensabor.entities.TipoPromocion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromocionResponseDTO {
    private Long id;
    private String denominacion;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private LocalTime horaDesde;
    private LocalTime horaHasta;
    private String descripcionDescuento;
    private TipoPromocion tipoPromocion;
    private TipoDescuento tipoDescuento;
    private Double valorDescuento;
    private Boolean activo;
    private Integer cantidadMinima;
    private EstadoPromocion estado;
    private List<PromocionDetalleResponseDTO> detalles;
    private List<ImagenDTO> imagenes;

    // ✅ Resumen financiero — calculado en PromocionServiceImpl.postCalcular()
    private Double precioOriginal; // Suma precioVenta * cantidad de todos los artículos
    private Double precioFinal; // precioOriginal con descuento aplicado
    private Double ahorro; // precioOriginal - precioFinal
    private Double totalCosto; // Suma costo * cantidad (costoProduccion o precioCompra)
    private Double gananciaEstimada; // precioFinal - totalCosto
    private Double margenGanancia; // (gananciaEstimada / precioFinal) * 100
}
