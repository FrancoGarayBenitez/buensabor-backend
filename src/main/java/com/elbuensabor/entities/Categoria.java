package com.elbuensabor.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categoria")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_categoria")
    private Long idCategoria;

    @Column(nullable = false)
    private String denominacion;

    @Column(nullable = false)
    private boolean esSubcategoria;

    @OneToMany(mappedBy = "categoria")
    private List<Articulo> articulos = new ArrayList<>();

    // Autorrelación: padre
    @ManyToOne
    @JoinColumn(name = "id_categoria_padre")
    private Categoria categoriaPadre;

    // Autorrelación: hijos
    @OneToMany(mappedBy = "categoriaPadre")
    private List<Categoria> subcategorias = new ArrayList<>();

    @ManyToMany(mappedBy = "categorias")
    private List<SucursalEmpresa> sucursales = new ArrayList<>();
}
