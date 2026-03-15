package com.elbuensabor.dto.response.cliente;

import com.elbuensabor.dto.request.ImagenDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * DTO simplificado de promoción para el cliente.
 * Muestra solo lo necesario para informar sobre la oferta.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromocionClienteDTO {
    private Long idPromocion;
    private String nombre;
    private String descripcion;

    // Vigencia
    private LocalDateTime fechaDesde;
    private LocalDateTime fechaHasta;
    private LocalTime horaDesde;
    private LocalTime horaHasta;

    // Descuento
    private String tipoDescuento;
    private Double valorDescuento;
    private String textoDescuento;

    // Precios calculados
    private Double precioOriginal;
    private Double precioFinal;
    private Integer porcentajeDescuento;

    // Condiciones
    private Integer cantidadMinima;
    private String mensajeCondiciones;

    // Artículos incluidos
    private Boolean esCombo;
    private List<ArticuloPromocionDTO> articulosIncluidos;

    // Imagen
    private ImagenDTO imagenPromocion;

    // ✅ NUEVOS CAMPOS - Estado de disponibilidad
    private Boolean disponibleAhora; // true si aplica en este momento exacto
    private String textoHorario; // "Disponible de 15:00 a 20:59"
}