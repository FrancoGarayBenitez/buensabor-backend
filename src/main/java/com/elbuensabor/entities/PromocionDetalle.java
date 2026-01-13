package com.elbuensabor.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "promocion_detalle")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromocionDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_promocion_detalle")
    private Long idPromocionDetalle;

    @Column(nullable = false)
    private int cantidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_articulo")
    private Articulo articulo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_promocion")
    private Promocion promocion;
}