package com.elbuensabor.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "compra_insumo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompraInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_articulo_insumo")
    private ArticuloInsumo insumo;

    private Double cantidad;

    private Double precioUnitario;

    private LocalDate fechaCompra = LocalDate.now();
}
