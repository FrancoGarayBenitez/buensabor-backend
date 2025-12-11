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
    @Column(name = "id_categoria")
    private Long idCategoria;

    @Column(nullable = false)
    private String denominacion;

    @Column(nullable = false)
    private boolean esSubcategoria;

    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
    private List<Articulo> articulos = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_categoria", nullable = false, length = 20)
    private TipoCategoria tipoCategoria;

    // Autorrelación: padre
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria_padre")
    private Categoria categoriaPadre;

    // Autorrelación: hijos
    @OneToMany(mappedBy = "categoriaPadre", fetch = FetchType.LAZY)
    private List<Categoria> subcategorias = new ArrayList<>();

    @ManyToMany(mappedBy = "categorias", fetch = FetchType.LAZY)
    private List<SucursalEmpresa> sucursales = new ArrayList<>();
}
