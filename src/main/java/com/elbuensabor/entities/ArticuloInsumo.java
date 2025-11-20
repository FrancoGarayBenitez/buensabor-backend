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
    private Double stockActual;

    @Column(name = "stock_maximo", nullable = false)
    private Double stockMaximo;

    @Column(name = "es_para_elaborar", nullable = false)
    private boolean esParaElaborar;

    @Column(name = "estado_stock", nullable = false, length = 20)
    private String estadoStock = "NORMAL";

    @OneToMany(mappedBy = "articuloInsumo")
    private List<ArticuloManufacturadoDetalle> detallesManufacturados = new ArrayList<>();

    @OneToMany(mappedBy = "articuloInsumo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<HistoricoPrecio> historicosPrecios = new ArrayList<>();

    @OneToMany(mappedBy = "articuloInsumo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CompraInsumo> compras = new ArrayList<>();
}
