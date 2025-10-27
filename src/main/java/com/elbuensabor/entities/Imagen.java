package com.elbuensabor.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "imagen")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Imagen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_imagen")
    private Long idImagen;

    @Column(nullable = false)
    private String denominacion;

    @Column(nullable = false)
    private String url;

    @ManyToOne
    @JoinColumn(name = "id_articulo")
    private Articulo articulo;

    @ManyToOne
    @JoinColumn(name = "id_promocion")
    private Promocion promocion;

    @OneToOne(mappedBy = "imagen")
    private Cliente cliente;
}
