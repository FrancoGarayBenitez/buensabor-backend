package com.elbuensabor.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="articulo_manufacturado")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticuloManufacturado extends Articulo{

    @Column(length = 1000)
    private String descripcion;

    @Column(name = "tiempo_estimado")
    private Integer tiempoEstimadoEnMinutos;

    @Column
    private String preparacion;

    @Column(name = "margen_ganancia")
    private Double margenGanancia;


    @OneToMany(mappedBy = "articuloManufacturado", cascade = CascadeType.ALL)
    private List<ArticuloManufacturadoDetalle> detalles = new ArrayList<>();
}
