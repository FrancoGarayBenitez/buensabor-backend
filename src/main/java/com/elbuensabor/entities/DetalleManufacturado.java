package com.elbuensabor.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "detalle_manufacturado")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleManufacturado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_manufacturado")
    private Long idDetalleManufacturado;

    @Column(nullable = false)
    private Double cantidad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_articulo_manufacturado", nullable = false)
    private ArticuloManufacturado articuloManufacturado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_articulo_insumo", nullable = false)
    private ArticuloInsumo articuloInsumo;
}
