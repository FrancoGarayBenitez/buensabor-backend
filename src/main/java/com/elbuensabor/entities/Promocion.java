package com.elbuensabor.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "promocion", indexes = {
        @Index(name = "ix_promocion_fechas", columnList = "fecha_desde, fecha_hasta"),
        @Index(name = "ix_promocion_eliminado", columnList = "eliminado")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Promocion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_promocion")
    private Long idPromocion;

    @Column(nullable = false)
    private String denominacion;

    @Column(name = "fecha_desde", nullable = false)
    private LocalDateTime fechaDesde;

    @Column(name = "fecha_hasta", nullable = false)
    private LocalDateTime fechaHasta;

    @Column(name = "hora_desde", nullable = false)
    private LocalTime horaDesde;

    @Column(name = "hora_hasta", nullable = false)
    private LocalTime horaHasta;

    @Column(name = "descripcion_descuento", length = 500)
    private String descripcionDescuento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_promocion", nullable = false)
    private TipoPromocion tipoPromocion = TipoPromocion.COMBO;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_descuento", nullable = false)
    private TipoDescuento tipoDescuento = TipoDescuento.PORCENTUAL;

    @Column(name = "valor_descuento", nullable = false)
    private Double valorDescuento; // % si es PORCENTUAL, $ si es MONTO_FIJO

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "cantidad_minima", nullable = false)
    private Integer cantidadMinima = 1;

    @Column(nullable = false)
    private Boolean eliminado = false;

    // ✅ RELACIONES
    @OneToMany(mappedBy = "promocion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PromocionDetalle> detalles = new ArrayList<>();

    @OneToMany(mappedBy = "promocion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Imagen> imagenes = new ArrayList<>();

    // ==================== LÓGICA DE NEGOCIO ====================

    /**
     * Verifica si la promoción está actualmente en un período válido (fecha y
     * hora).
     * No considera el estado 'activo' o 'eliminado'.
     * 
     * @return true si la fecha y hora actual están dentro del rango de la
     *         promoción.
     */
    public boolean estaEnPeriodoValido() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalTime horaActual = ahora.toLocalTime();

        return !ahora.isBefore(fechaDesde) && !ahora.isAfter(fechaHasta) &&
                !horaActual.isBefore(horaDesde) && !horaActual.isAfter(horaHasta);
    }

    /**
     * Verifica si la promoción está vigente y puede ser aplicada.
     * Considera el estado 'activo', 'eliminado' y el período de validez.
     * 
     * @return true si la promoción es aplicable ahora mismo.
     */
    public boolean estaVigente() {
        return this.activo && !this.eliminado && estaEnPeriodoValido();
    }

    /**
     * Calcula y devuelve el estado actual de la promoción.
     * Útil para la UI del administrador.
     * 
     * @return El enum EstadoPromocion correspondiente.
     */
    @Transient
    public EstadoPromocion getEstado() {
        if (!this.activo) {
            return EstadoPromocion.INACTIVA;
        }
        LocalDateTime ahora = LocalDateTime.now();
        if (ahora.isBefore(this.fechaDesde)) {
            return EstadoPromocion.PROGRAMADA;
        }
        if (ahora.isAfter(this.fechaHasta)) {
            return EstadoPromocion.EXPIRADA;
        }
        return EstadoPromocion.VIGENTE;
    }

    public boolean aplicaParaArticulo(Long idArticulo) {
        return detalles.stream()
                .anyMatch(detalle -> detalle.getArticulo().getIdArticulo().equals(idArticulo));
    }

    public Double calcularDescuento(Double precioOriginal, Integer cantidad) {
        if (!estaVigente() || cantidad < cantidadMinima) {
            return 0.0;
        }

        Double descuentoBase = switch (tipoDescuento) {
            case PORCENTUAL -> precioOriginal * (valorDescuento / 100);
            case MONTO_FIJO -> valorDescuento;
        };

        return Math.min(descuentoBase * cantidad, precioOriginal * cantidad);
    }

    public Double calcularPrecioConDescuento(Double precioOriginal, Integer cantidad) {
        return Math.max(0, (precioOriginal * cantidad) - calcularDescuento(precioOriginal, cantidad));
    }
}
