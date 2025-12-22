package com.elbuensabor.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "articulo", uniqueConstraints = {
        @UniqueConstraint(name = "ux_articulo_denominacion", columnNames = "denominacion")
}, indexes = {
        @Index(name = "ix_articulo_eliminado", columnList = "eliminado")
})
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
    private Boolean eliminado = false;

    // ✅ RELACIONES COMPARTIDAS
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_unidad_medida", nullable = false)
    private UnidadMedida unidadMedida;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @OneToMany(mappedBy = "articulo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Imagen> imagenes = new ArrayList<>();

    @ManyToMany(mappedBy = "articulos", fetch = FetchType.LAZY)
    private List<Promocion> promociones = new ArrayList<>();

    // ==================== MÉTODOS SIMPLES - Lógica de Dominio ====================

    // Consulta de estado (necesario para reglas de negocio)
    public boolean tienePromocionVigente() {
        return promociones.stream().anyMatch(Promocion::estaVigente);
    }

    // Obtener promoción activa (necesario para cálculos)
    public Promocion getPromocionVigente() {
        return promociones.stream()
                .filter(Promocion::estaVigente)
                .findFirst()
                .orElse(null);
    }

    // Métodos de validación
    public boolean puedeEliminarse() {
        return !this.eliminado;
    }

    public void marcarEliminado() {
        this.eliminado = true;
    }
}
