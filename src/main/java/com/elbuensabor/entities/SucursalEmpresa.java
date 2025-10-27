package com.elbuensabor.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="sucursal")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SucursalEmpresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_sucursal")
    private Long idSucursalEmpresa;

    @Column(nullable = false)
    private String nombre;

    @Column(name="horario_apertura", nullable = false)
    private LocalTime horarioApertura;

    @Column(name = "horario_cierre", nullable = false)
    private LocalTime horarioCierre;

    @ManyToOne
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    @OneToOne
    @JoinColumn(name = "id_domicilio")
    private Domicilio domicilio;

    @ManyToMany
    @JoinTable(
            name = "sucursal_promocion",
            joinColumns = @JoinColumn(name = "id_sucursal"),
            inverseJoinColumns = @JoinColumn(name = "id_promocion")
    )
    private List<Promocion> promociones = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "sucursal_categoria",
            joinColumns = @JoinColumn(name = "id_sucursal"),
            inverseJoinColumns = @JoinColumn(name = "id_categoria")
    )
    private List<Categoria> categorias = new ArrayList<>();

    @OneToMany(mappedBy = "sucursal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pedido> pedidos = new ArrayList<>();
}
