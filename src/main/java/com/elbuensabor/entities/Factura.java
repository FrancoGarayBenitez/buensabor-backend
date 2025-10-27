package com.elbuensabor.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "factura")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Factura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_factura")
    private Long idFactura;

    @Column(nullable = false)
    private LocalDate fechaFactura;

    @Column(name = "nro_comprobante", nullable = false, unique = true)
    private String nroComprobante;

    @Column(nullable = false)
    private Double subTotal;

    @Column(nullable = false)
    private Double descuento;

    @Column(name = "gastos_envio", nullable = false)
    private Double gastosEnvio;

    @Column(name="total_venta", nullable = false)
    private Double totalVenta;

    // ✅ SOLUCIÓN DEFINITIVA: Ignorar en JSON para evitar recursión
    @OneToOne
    @JoinColumn(name = "id_pedido")
    @JsonIgnore  // ✅ Completamente ignorado en serialización
    private Pedido pedido;

    // ✅ SOLUCIÓN DEFINITIVA: Ignorar pagos en JSON
    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore  // ✅ Los pagos se mapearán manualmente en el DTO
    private List<Pago> pagos = new ArrayList<>();

    // Métodos de conveniencia
    public void addPago(Pago pago) {
        pagos.add(pago);
        pago.setFactura(this);
    }

    public void removePago(Pago pago) {
        pagos.remove(pago);
        pago.setFactura(null);
    }

    // ✅ toString() personalizado SIN referencias circulares
    @Override
    public String toString() {
        return "Factura{" +
                "idFactura=" + idFactura +
                ", fechaFactura=" + fechaFactura +
                ", nroComprobante='" + nroComprobante + '\'' +
                ", subTotal=" + subTotal +
                ", descuento=" + descuento +
                ", gastosEnvio=" + gastosEnvio +
                ", totalVenta=" + totalVenta +
                ", pedidoId=" + (pedido != null ? pedido.getIdPedido() : null) +
                '}';
    }

    // ✅ equals() y hashCode() seguros
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Factura)) return false;
        Factura factura = (Factura) o;
        return idFactura != null && idFactura.equals(factura.idFactura);
    }

    @Override
    public int hashCode() {
        return idFactura != null ? idFactura.hashCode() : 0;
    }
}