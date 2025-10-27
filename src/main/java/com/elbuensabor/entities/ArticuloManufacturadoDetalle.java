package com.elbuensabor.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="manufacturado_detalle")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticuloManufacturadoDetalle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_manufacturado_detalle")
    private Long idDetalleManufacturado;

    @Column(nullable = false)
    private Double cantidad;

    @ManyToOne
    @JoinColumn(name = "id_articulo_manufacturado", nullable = false)
    private ArticuloManufacturado articuloManufacturado;

    @ManyToOne
    @JoinColumn(name = "id_articulo_insumo", nullable = false)
    private ArticuloInsumo articuloInsumo;
}
