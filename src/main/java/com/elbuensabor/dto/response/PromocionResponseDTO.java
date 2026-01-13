package com.elbuensabor.dto.response;

import com.elbuensabor.dto.request.ImagenDTO;
import com.elbuensabor.entities.EstadoPromocion;
import com.elbuensabor.entities.TipoDescuento;
import com.elbuensabor.entities.TipoPromocion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromocionResponseDTO {
    private Long id;
    private String denominacion;
    private LocalDateTime fechaDesde;
    private LocalDateTime fechaHasta;
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
}
