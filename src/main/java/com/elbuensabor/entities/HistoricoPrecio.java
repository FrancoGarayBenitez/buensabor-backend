package com.elbuensabor.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "historico_precio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoPrecio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historico_precio")
    private Long idHistoricoPrecio;

    @ManyToOne
    @JoinColumn(name = "id_articulo_insumo", nullable = false)
    private ArticuloInsumo articuloInsumo;

    @Column(name = "precio_unitario", nullable = false)
    private Double precioUnitario;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "cantidad")
    private Double cantidad;

    @PrePersist
    public void prePersist() {
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
    }
}
