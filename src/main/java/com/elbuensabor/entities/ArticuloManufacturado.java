package com.elbuensabor.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "articulo_manufacturado")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticuloManufacturado extends Articulo {

    @Column(length = 1000)
    private String descripcion;

    @Column(name = "tiempo_estimado_minutos", nullable = false)
    private Integer tiempoEstimadoEnMinutos = 0;

    @Column(name = "preparacion", length = 4000) // Aumentado para preparaciones largas
    private String preparacion;

    @Column(name = "margen_ganancia", nullable = false)
    private Double margenGanancia = 1.0; // Multiplicador (ej: 1.3 = 30% de margen)

    @Column(name = "costo_produccion", nullable = false)
    private Double costoProduccion = 0.0;

    @OneToMany(mappedBy = "articuloManufacturado", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DetalleManufacturado> detalles = new ArrayList<>();

    // ==================== LÓGICA DE NEGOCIO (REFACTORIZADA) ====================

    /**
     * Recalcula el costo de producción basado en el precio actual de los
     * ingredientes
     * y actualiza el campo 'costoProduccion'.
     * 
     * @return El nuevo costo de producción.
     */
    public Double actualizarCostoProduccion() {
        Double nuevoCosto = detalles.stream()
                .mapToDouble(detalle -> detalle.getArticuloInsumo().getPrecioCompra() * detalle.getCantidad())
                .sum();
        this.costoProduccion = Math.round(nuevoCosto * 100.0) / 100.0; // Redondeo a 2 decimales
        return this.costoProduccion;
    }

    /**
     * Actualiza el precio de venta basado en el costo de producción recalculado y
     * el margen de ganancia.
     * 
     * @return El nuevo precio de venta.
     */
    public Double actualizarPrecioVenta() {
        actualizarCostoProduccion(); // Asegura que el costo es el más reciente
        Double nuevoPrecio = this.costoProduccion * this.margenGanancia;
        this.setPrecioVenta(Math.round(nuevoPrecio * 100.0) / 100.0);
        return this.getPrecioVenta();
    }

    /**
     * Calcula la ganancia neta por unidad (Precio de Venta - Costo de Producción).
     * 
     * @return La ganancia.
     */
    public Double calcularGanancia() {
        if (this.getPrecioVenta() == null || this.costoProduccion == null) {
            return 0.0;
        }
        return this.getPrecioVenta() - this.costoProduccion;
    }

    /**
     * Convierte el margen multiplicador (ej: 1.3) a un porcentaje (ej: 30.0).
     * 
     * @return El margen en formato de porcentaje.
     */
    public Double getMargenGananciaPorcentaje() {
        return (this.margenGanancia - 1) * 100;
    }

    /**
     * Fija el margen multiplicador a partir de un porcentaje.
     * 
     * @param porcentaje El margen en porcentaje (ej: 30 para un 30%).
     */
    public void setMargenGananciaPorcentaje(Double porcentaje) {
        if (porcentaje == null || porcentaje < 0) {
            this.margenGanancia = 1.0;
        } else {
            this.margenGanancia = 1 + (porcentaje / 100);
        }
    }

    // ==================== LÓGICA DE STOCK ====================

    /**
     * Verifica si hay stock suficiente de todos los ingredientes para producir una
     * cantidad dada.
     * 
     * @param cantidadAProducir El número de unidades del producto a fabricar.
     * @return true si hay stock suficiente, false en caso contrario.
     */
    public boolean verificarStockSuficiente(int cantidadAProducir) {
        if (detalles == null || detalles.isEmpty()) {
            return true; // No requiere ingredientes
        }
        return detalles.stream().allMatch(
                detalle -> detalle.getArticuloInsumo().getStockActual() >= detalle.getCantidad() * cantidadAProducir);
    }

    /**
     * Calcula la cantidad máxima de este producto que se puede preparar con el
     * stock actual de ingredientes.
     * 
     * @return El número máximo de unidades fabricables.
     */
    public Integer calcularCantidadMaximaPreparable() {
        if (detalles == null || detalles.isEmpty()) {
            return Integer.MAX_VALUE; // No depende de stock de insumos
        }
        return detalles.stream()
                .mapToInt(detalle -> (int) Math
                        .floor(detalle.getArticuloInsumo().getStockActual() / detalle.getCantidad()))
                .min()
                .orElse(0);
    }
}
