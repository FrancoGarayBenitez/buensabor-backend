package com.elbuensabor.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "imagen")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @JsonIgnore
    private Articulo articulo;

    @ManyToOne
    @JoinColumn(name = "id_promocion")
    @JsonIgnore
    private Promocion promocion;

    @OneToOne(mappedBy = "imagen")
    private Cliente cliente;
}
