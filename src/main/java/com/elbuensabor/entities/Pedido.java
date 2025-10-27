package com.elbuensabor.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Long idPedido;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(name = "hora_estimada_finalizacion")
    private LocalTime horaEstimadaFinalizacion;

    @Column(nullable = false)
    private Double total;

    @Column(name = "total_costo", nullable = false)
    private Double totalCosto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_envio", nullable = false)
    private TipoEnvio tipoEnvio;

    // ✅ NUEVO CAMPO: Observaciones generales del pedido
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_domicilio")
    private Domicilio domicilio;

    @ManyToOne
    @JoinColumn(name = "id_sucursal", nullable = false)
    private SucursalEmpresa sucursal;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    private List<DetallePedido> detalles = new ArrayList<>();

    // ✅ SOLUCIÓN DEFINITIVA: Ignorar factura en JSON
    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL)
    @JsonIgnore  // ✅ Evita recursión - la factura se maneja por separado
    private Factura factura;

    // ✅ toString() personalizado SIN referencias circulares
    @Override
    public String toString() {
        return "Pedido{" +
                "idPedido=" + idPedido +
                ", fecha=" + fecha +
                ", horaEstimadaFinalizacion=" + horaEstimadaFinalizacion +
                ", total=" + total +
                ", totalCosto=" + totalCosto +
                ", estado=" + estado +
                ", tipoEnvio=" + tipoEnvio +
                ", clienteId=" + (cliente != null ? cliente.getIdCliente() : null) +
                ", sucursalId=" + (sucursal != null ? sucursal.getIdSucursalEmpresa() : null) +
                ", detallesCount=" + (detalles != null ? detalles.size() : 0) +
                '}';
    }

    // ✅ equals() y hashCode() seguros
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pedido)) return false;
        Pedido pedido = (Pedido) o;
        return idPedido != null && idPedido.equals(pedido.idPedido);
    }

    @Override
    public int hashCode() {
        return idPedido != null ? idPedido.hashCode() : 0;
    }
}