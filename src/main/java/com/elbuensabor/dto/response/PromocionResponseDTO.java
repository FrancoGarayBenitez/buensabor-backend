package com.elbuensabor.dto.response;

import com.elbuensabor.entities.TipoDescuento;
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

    private Long idPromocion;
    private String denominacion;
    private LocalDateTime fechaDesde;
    private LocalDateTime fechaHasta;
    private LocalTime horaDesde;
    private LocalTime horaHasta;
    private String descripcionDescuento;
    private TipoDescuento tipoDescuento;
    private Double valorDescuento;
    private Double precioPromocional;
    private Integer cantidadMinima;
    private Boolean activo;

    // Información de artículos incluidos
    private List<ArticuloSimpleDTO> articulos;

    // Estado calculado
    private Boolean estaVigente;
    private String estadoDescripcion;

    // Información de imágenes
    private List<String> urlsImagenes;

    // DTO interno para artículos
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArticuloSimpleDTO {
        private Long idArticulo;
        private String denominacion;
        private Double precioVenta;
        private String urlImagen;
    }
}