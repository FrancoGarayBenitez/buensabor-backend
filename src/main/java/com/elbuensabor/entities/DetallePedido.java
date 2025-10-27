package com.elbuensabor.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="detalle_pedido")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetallePedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_pedido")
    private Long idDetallePedido;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private Double subtotal;

    @ManyToOne
    @JoinColumn(name = "id_articulo", nullable = false)
    private Articulo articulo;

    @ManyToOne
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    // ✅ NUEVOS CAMPOS PARA PROMOCIONES
    @Column(name = "precio_unitario_original", nullable = false)
    private Double precioUnitarioOriginal; // Precio sin promoción

    @Column(name = "descuento_promocion")
    private Double descuentoPromocion = 0.0; // Monto del descuento aplicado

    @ManyToOne
    @JoinColumn(name = "id_promocion")
    private Promocion promocionAplicada; // Promoción que se aplicó (si existe)

    // ✅ MÉTODOS DE UTILIDAD
    public Double getPrecioUnitarioFinal() {
        return precioUnitarioOriginal - (descuentoPromocion / cantidad);
    }

    public Double getSubtotalOriginal() {
        return precioUnitarioOriginal * cantidad;
    }

    public Boolean tienePromocion() {
        return promocionAplicada != null && descuentoPromocion > 0;
    }

    public String getResumenDescuento() {
        if (!tienePromocion()) {
            return null;
        }

        return String.format("Promoción '%s': -$%.2f",
                promocionAplicada.getDenominacion(),
                descuentoPromocion);
    }
}