package com.elbuensabor.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "articulo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Articulo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_articulo")
    private Long idArticulo;

    @Column(nullable = false)
    private String denominacion;

    @Column(name = "precio_venta", nullable = false)
    private Double precioVenta;

    @Column(nullable = false)
    private Boolean eliminado = false;  // <---- AGREGADO

    @ManyToOne
    @JoinColumn(name = "id_unidad_medida")
    private UnidadMedida unidadMedida;

    @OneToMany(mappedBy = "articulo", cascade = CascadeType.ALL)
    private List<Imagen> imagenes = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    @OneToMany(mappedBy = "articulo")
    private List<DetallePedido> detallesPedido = new ArrayList<>();

    @ManyToMany(mappedBy = "articulos")
    private List<Promocion> promociones = new ArrayList<>();
}
