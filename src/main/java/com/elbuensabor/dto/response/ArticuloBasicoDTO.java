package com.elbuensabor.dto.response;

public class ArticuloBasicoDTO {
    private Long idArticulo;
    private String denominacion;
    private Double precioVenta;
    private String imagenUrl;

    // Constructores
    public ArticuloBasicoDTO() {}

    public ArticuloBasicoDTO(Long idArticulo, String denominacion, Double precioVenta, String imagenUrl) {
        this.idArticulo = idArticulo;
        this.denominacion = denominacion;
        this.precioVenta = precioVenta;
        this.imagenUrl = imagenUrl;
    }

    // Getters y Setters
    public Long getIdArticulo() {
        return idArticulo;
    }

    public void setIdArticulo(Long idArticulo) {
        this.idArticulo = idArticulo;
    }

    public String getDenominacion() {
        return denominacion;
    }

    public void setDenominacion(String denominacion) {
        this.denominacion = denominacion;
    }

    public Double getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(Double precioVenta) {
        this.precioVenta = precioVenta;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }
}