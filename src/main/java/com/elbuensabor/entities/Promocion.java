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
    private Double valorDescuento;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "cantidad_minima", nullable = false)
    private Integer cantidadMinima = 1;

    @Column(nullable = false)
    private Boolean eliminado = false;

    @OneToMany(mappedBy = "promocion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PromocionDetalle> detalles = new ArrayList<>();

    @OneToMany(mappedBy = "promocion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Imagen> imagenes = new ArrayList<>();

    // ==================== LÓGICA DE NEGOCIO ====================

    /**
     * ✅ Verifica si la fecha actual está dentro del rango de fechas de la
     * promoción.
     * NO considera la hora, solo fechas.
     */
    public boolean estaEnRangoFechas() {
        LocalDateTime ahora = LocalDateTime.now();
        return !ahora.isBefore(fechaDesde) && !ahora.isAfter(fechaHasta);
    }

    /**
     * ✅ Verifica si la hora actual está dentro del rango horario de la promoción.
     * Maneja correctamente horarios nocturnos (ej: 22:00 a 02:00).
     */
    public boolean estaEnRangoHorario() {
        LocalTime horaActual = LocalTime.now();

        // Si horaDesde <= horaHasta: horario normal (ej: 10:00 a 22:00)
        if (!horaDesde.isAfter(horaHasta)) {
            return !horaActual.isBefore(horaDesde) && !horaActual.isAfter(horaHasta);
        } else {
            // Horario nocturno que cruza medianoche (ej: 22:00 a 02:00)
            // Está válido si: horaActual >= horaDesde OR horaActual <= horaHasta
            return !horaActual.isBefore(horaDesde) || !horaActual.isAfter(horaHasta);
        }
    }

    /**
     * ✅ Verifica si la promoción está en período válido (fecha Y hora).
     * Usado para aplicar descuentos en el carrito.
     */
    public boolean estaEnPeriodoValido() {
        return estaEnRangoFechas() && estaEnRangoHorario();
    }

    /**
     * ✅ NUEVO: Verifica si la promoción debe mostrarse en el catálogo.
     * Solo verifica fechas, NO horario - para mostrar todas las promos del día.
     * El cliente verá el horario en la UI.
     */
    public boolean estaVigenteParaMostrar() {
        return this.activo && !this.eliminado && estaEnRangoFechas();
    }

    /**
     * ✅ Verifica si la promoción está vigente Y aplica AHORA MISMO.
     * Considera activo, eliminado, fecha Y hora.
     * Usado para aplicar descuentos en el carrito.
     */
    public boolean estaVigente() {
        return this.activo && !this.eliminado && estaEnPeriodoValido();
    }

    /**
     * ✅ Calcula y devuelve el estado actual de la promoción.
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

    /**
     * ✅ NUEVO: Verifica si la promoción aplica en este momento exacto.
     * Útil para mostrar badges como "¡Disponible ahora!" o "Disponible de X a Y"
     */
    @Transient
    public boolean estaDisponibleAhora() {
        return estaVigente();
    }

    /**
     * ✅ NUEVO: Genera texto legible del horario de la promoción.
     * Ej: "Disponible de 15:00 a 20:59"
     */
    @Transient
    public String getTextoHorario() {
        return String.format("Disponible de %s a %s",
                horaDesde.toString(),
                horaHasta.toString());
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
