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

    @Column(name = "preparacion", length = 2000)
    private String preparacion;

    @Column(name = "margen_ganancia", nullable = false)
    private Double margenGanancia = 1.0;

    @Column(name = "costo_produccion", nullable = false)
    private Double costoProduccion = 0.0;

    @OneToMany(mappedBy = "articuloManufacturado", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DetalleManufacturado> detalles = new ArrayList<>();

    // ==================== MÉTODOS SIMPLES ====================

    // Cálculo de ganancia (lógica de dominio)
    public Double calcularGanancia() {
        if (this.getPrecioVenta() == null || this.costoProduccion == null) {
            return 0.0;
        }
        return (this.getPrecioVenta() - this.costoProduccion) * this.margenGanancia;
    }

    // Calcular margen porcentual
    public Double calcularMargenPorcentaje() {
        if (this.costoProduccion == null || this.costoProduccion == 0) {
            return 0.0;
        }
        return ((this.getPrecioVenta() - this.costoProduccion) / this.costoProduccion) * 100;
    }

    // Verificar si hay detalles (es una receta completa)
    public boolean tieneReceta() {
        return detalles != null && !detalles.isEmpty();
    }

    // Contar ingredientes únicos
    public Integer contarIngredientes() {
        return detalles != null ? detalles.size() : 0;
    }

    // Calcular costo total de ingredientes
    public Double calcularCostoIngredientes() {
        return detalles.stream()
                .mapToDouble(detalle -> detalle.getArticuloInsumo().getPrecioCompra() * detalle.getCantidad())
                .sum();
    }

    // Validar consistencia de costos
    public boolean esCostoProduccionValido() {
        Double costoCalculado = calcularCostoIngredientes();
        // Permitir pequeñas variaciones (5%)
        Double tolerancia = costoCalculado * 0.05;
        return Math.abs(this.costoProduccion - costoCalculado) <= tolerancia;
    }
}
