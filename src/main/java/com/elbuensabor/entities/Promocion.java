package com.elbuensabor.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "promocion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Promocion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_promocion")
    private Long idPromocion;

    @Column(nullable = false)
    private String denominacion;

    @Column(name = "fecha_desde", nullable = false)
    private LocalDateTime fechaDesde;

    @Column(name = "fecha_hasta", nullable = false)
    private LocalDateTime fechaHasta;

    @Column(name = "hora_desde", nullable = false)
    private LocalTime horaDesde;

    @Column(name = "hora_hasta", nullable = false)
    private LocalTime horaHasta;

    @Column(name = "descripcion_descuento", length = 500)
    private String descripcionDescuento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_descuento", nullable = false)
    private TipoDescuento tipoDescuento = TipoDescuento.PORCENTUAL;

    @Column(name = "valor_descuento", nullable = false)
    private Double valorDescuento; // % si es PORCENTUAL, $ si es MONTO_FIJO

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "cantidad_minima", nullable = false)
    private Integer cantidadMinima = 1;

    // ✅ RELACIONES
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "promocion_articulo", joinColumns = @JoinColumn(name = "id_promocion"), inverseJoinColumns = @JoinColumn(name = "id_articulo"))
    private List<Articulo> articulos = new ArrayList<>();

    @OneToMany(mappedBy = "promocion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Imagen> imagenes = new ArrayList<>();

    @ManyToMany(mappedBy = "promociones", fetch = FetchType.LAZY)
    private List<SucursalEmpresa> sucursales = new ArrayList<>();

    // ✅ ENUMS
    public enum TipoDescuento {
        PORCENTUAL,
        MONTO_FIJO
    }

    // ✅ MÉTODOS DE UTILIDAD
    public boolean estaVigente() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalTime horaActual = LocalTime.now();

        return activo &&
                ahora.isAfter(fechaDesde) &&
                ahora.isBefore(fechaHasta) &&
                horaActual.isAfter(horaDesde) &&
                horaActual.isBefore(horaHasta);
    }

    public boolean aplicaParaArticulo(Long idArticulo) {
        return articulos.stream()
                .anyMatch(articulo -> articulo.getIdArticulo().equals(idArticulo));
    }

    public boolean aplicaParaSucursal(Long idSucursal) {
        return sucursales.stream()
                .anyMatch(sucursal -> sucursal.getIdSucursalEmpresa().equals(idSucursal));
    }

    public Double calcularDescuento(Double precioOriginal, Integer cantidad) {
        if (!estaVigente() || cantidad < cantidadMinima) {
            return 0.0;
        }

        Double descuentoBase = switch (tipoDescuento) {
            case PORCENTUAL -> precioOriginal * (valorDescuento / 100);
            case MONTO_FIJO -> valorDescuento;
        };

        return Math.min(descuentoBase * cantidad, precioOriginal * cantidad);
    }

    public Double calcularPrecioConDescuento(Double precioOriginal, Integer cantidad) {
        return Math.max(0, (precioOriginal * cantidad) - calcularDescuento(precioOriginal, cantidad));
    }
}
