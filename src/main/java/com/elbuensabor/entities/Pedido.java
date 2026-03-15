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

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pago", nullable = false)
    private FormaPago formaPago;

    @Column(name = "pago_confirmado", nullable = false)
    private Boolean pagoConfirmado = false;

    @Column(name = "fecha_confirmacion_pago")
    private LocalDateTime fechaConfirmacionPago;

    @ManyToOne
    @JoinColumn(name = "id_usuario_confirma_pago")
    private Usuario usuarioConfirmaPago; // Cajero que confirma efectivo

    @Column(name = "codigo_mp")
    private String codigoMercadoPago; // ID de transacción de MercadoPago

    @Column(name = "fecha_inicio_preparacion")
    private LocalDateTime fechaInicioPreparacion;

    @Column(name = "fecha_listo")
    private LocalDateTime fechaListo;

    @Column(name = "fecha_entregado")
    private LocalDateTime fechaEntregado;

    @Column(name = "fecha_cancelado")
    private LocalDateTime fechaCancelado;

    @Column(name = "motivo_cancelacion", columnDefinition = "TEXT")
    private String motivoCancelacion;

    @ManyToOne
    @JoinColumn(name = "id_usuario_cancela")
    private Usuario usuarioCancela; // Cliente, Cajero o Admin que cancela

    @ManyToOne
    @JoinColumn(name = "id_usuario_delivery")
    private Usuario usuarioDelivery; // Usuario con rol DELIVERY asignado

    @Column(name = "tiempo_extension_minutos")
    private Integer tiempoExtensionMinutos = 0;

    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_domicilio")
    private Domicilio domicilio;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL)
    @JsonIgnore
    private Factura factura;

    // ✅ MÉTODOS DE NEGOCIO SIMPLES

    public boolean puedeSerCancelado() {
        return estado != Estado.ENTREGADO && estado != Estado.CANCELADO;
    }

    public boolean requiereConfirmacionPago() {
        return formaPago == FormaPago.EFECTIVO && !pagoConfirmado;
    }

    public boolean puedeIniciarPreparacion() {
        return estado == Estado.PENDIENTE &&
                (formaPago == FormaPago.MERCADO_PAGO || pagoConfirmado);
    }

    public boolean puedeMarcarListo() {
        return estado == Estado.PREPARACION;
    }

    public boolean puedeEntregarse() {
        return estado == Estado.LISTO;
    }

    public boolean estaRetrasado() {
        if (estado != Estado.PREPARACION || horaEstimadaFinalizacion == null) {
            return false;
        }
        return LocalTime.now().isAfter(horaEstimadaFinalizacion);
    }

    public Double getTotalDescuentos() {
        return detalles.stream()
                .mapToDouble(DetallePedido::getDescuentoPromocion)
                .sum();
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "idPedido=" + idPedido +
                ", fecha=" + fecha +
                ", estado=" + estado +
                ", formaPago=" + formaPago +
                ", pagoConfirmado=" + pagoConfirmado +
                ", total=" + total +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Pedido))
            return false;
        Pedido pedido = (Pedido) o;
        return idPedido != null && idPedido.equals(pedido.idPedido);
    }

    @Override
    public int hashCode() {
        return idPedido != null ? idPedido.hashCode() : 0;
    }
}