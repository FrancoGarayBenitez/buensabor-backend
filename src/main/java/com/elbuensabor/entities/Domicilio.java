package com.elbuensabor.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="domicilio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Domicilio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_domicilio")
    private Long idDomicilio;

    @Column(nullable = false)
    private String calle;

    @Column(nullable = false)
    private Integer numero;

    @Column(nullable = false)
    private Integer cp;

    @Column(nullable = false)
    private String localidad;

    @Column(name = "es_principal", nullable = false)
    private Boolean esPrincipal = false;

    @OneToOne(mappedBy = "domicilio")
    private SucursalEmpresa sucursalEmpresa;

    @OneToMany(mappedBy = "domicilio", cascade = CascadeType.ALL)
    private List<Pedido> pedidos = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;
}
