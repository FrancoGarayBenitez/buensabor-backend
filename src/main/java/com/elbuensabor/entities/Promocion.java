package com.elbuensabor.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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

    @Column(name = "descripcion_descuento")
    private String descripcionDescuento;

    // ✅ NUEVO: Tipo de descuento
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_descuento", nullable = false)
    private TipoDescuento tipoDescuento = TipoDescuento.PORCENTUAL;

    // ✅ RENOMBRADO: Más claro para manejar ambos tipos
    @Column(name = "valor_descuento", nullable = false)
    private Double valorDescuento; // % si es PORCENTUAL, $ si es MONTO_FIJO

    // ✅ NUEVO: Precio final calculado automáticamente (opcional)
    @Column(name = "precio_promocional")
    private Double precioPromocional;

    // ✅ NUEVO: Estado activo/inactivo
    @Column(nullable = false)
    private Boolean activo = true;

    // ✅ NUEVO: Para promociones que requieren cantidad mínima
    @Column(name = "cantidad_minima")
    private Integer cantidadMinima = 1;

    @ManyToMany
    @JoinTable(
            name = "promocion_articulo",
            joinColumns = @JoinColumn(name = "id_promocion"),
            inverseJoinColumns = @JoinColumn(name = "id_articulo")
    )
    private List<Articulo> articulos = new ArrayList<>();

    @OneToMany(mappedBy = "promocion", cascade = CascadeType.ALL)
    private List<Imagen> imagenes = new ArrayList<>();

    @ManyToMany(mappedBy = "promociones")
    private List<SucursalEmpresa> sucursales = new ArrayList<>();

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

        switch (tipoDescuento) {
            case PORCENTUAL:
                return precioOriginal * cantidad * (valorDescuento / 100);
            case MONTO_FIJO:
                return Math.min(valorDescuento * cantidad, precioOriginal * cantidad);
            default:
                return 0.0;
        }
    }
}
