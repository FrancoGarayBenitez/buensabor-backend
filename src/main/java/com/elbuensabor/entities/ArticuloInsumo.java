package com.elbuensabor.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "articulo_insumo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticuloInsumo extends Articulo {

    @Column(name = "precio_compra", nullable = false)
    private Double precioCompra;

    @Column(name = "stock_actual", nullable = false)
    private Double stockActual = 0.0;

    @Column(name = "stock_maximo", nullable = false)
    private Double stockMaximo = 0.0;

    @Column(name = "es_para_elaborar", nullable = false)
    private Boolean esParaElaborar = false;

    @Column(name = "estado_stock", nullable = false, length = 20)
    private String estadoStock = "CRITICO";

    // ==================== RELACIONES ====================
    @OneToMany(mappedBy = "articuloInsumo", fetch = FetchType.LAZY)
    private List<DetalleManufacturado> detallesManufacturados = new ArrayList<>();

    @OneToMany(mappedBy = "articuloInsumo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistoricoPrecio> historicosPrecios = new ArrayList<>();

    @OneToMany(mappedBy = "articuloInsumo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompraInsumo> compras = new ArrayList<>();

    // ==================== MÉTODOS SIMPLES ====================

    // Verificar si hay stock disponible
    public boolean tieneStockDisponible(Double cantidad) {
        return this.stockActual >= cantidad;
    }

    // Calcular porcentaje de stock
    public Double getPorcentajeStock() {
        if (this.stockMaximo == 0) {
            return 0.0;
        }
        return (this.stockActual / this.stockMaximo) * 100;
    }

    // Calcular costo total del inventario
    public Double getCostoTotalInventario() {
        return this.precioCompra * this.stockActual;
    }

    // Calcular margen de ganancia
    public Double getMargenGanancia() {
        if (this.precioCompra == 0) {
            return 0.0;
        }
        return ((this.getPrecioVenta() - this.precioCompra) / this.precioCompra) * 100;
    }
}
