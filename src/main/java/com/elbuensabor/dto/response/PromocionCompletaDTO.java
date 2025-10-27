package com.elbuensabor.dto.response;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class PromocionCompletaDTO {
    private Long idPromocion;
    private String denominacion;
    private String descripcionDescuento;
    private LocalDateTime fechaDesde;
    private LocalDateTime fechaHasta;
    private LocalTime horaDesde;
    private LocalTime horaHasta;
    private String tipoDescuento;
    private Double valorDescuento;
    private Boolean activo;
    private List<ArticuloBasicoDTO> articulos;

    // Getters y Setters
    public Long getIdPromocion() { return idPromocion; }
    public void setIdPromocion(Long idPromocion) { this.idPromocion = idPromocion; }

    public String getDenominacion() { return denominacion; }
    public void setDenominacion(String denominacion) { this.denominacion = denominacion; }

    public String getDescripcionDescuento() { return descripcionDescuento; }
    public void setDescripcionDescuento(String descripcionDescuento) { this.descripcionDescuento = descripcionDescuento; }

    public LocalDateTime getFechaDesde() { return fechaDesde; }
    public void setFechaDesde(LocalDateTime fechaDesde) { this.fechaDesde = fechaDesde; }

    public LocalDateTime getFechaHasta() { return fechaHasta; }
    public void setFechaHasta(LocalDateTime fechaHasta) { this.fechaHasta = fechaHasta; }

    public LocalTime getHoraDesde() { return horaDesde; }
    public void setHoraDesde(LocalTime horaDesde) { this.horaDesde = horaDesde; }

    public LocalTime getHoraHasta() { return horaHasta; }
    public void setHoraHasta(LocalTime horaHasta) { this.horaHasta = horaHasta; }

    public String getTipoDescuento() { return tipoDescuento; }
    public void setTipoDescuento(String tipoDescuento) { this.tipoDescuento = tipoDescuento; }

    public Double getValorDescuento() { return valorDescuento; }
    public void setValorDescuento(Double valorDescuento) { this.valorDescuento = valorDescuento; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public List<ArticuloBasicoDTO> getArticulos() { return articulos; }
    public void setArticulos(List<ArticuloBasicoDTO> articulos) { this.articulos = articulos; }
}