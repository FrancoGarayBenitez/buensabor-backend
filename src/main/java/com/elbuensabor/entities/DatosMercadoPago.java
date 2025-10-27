package com.elbuensabor.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "datos_mercado_pago")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatosMercadoPago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_datos_mp")
    private Long idMercadoPago;

    // ✅ CAMBIO CRÍTICO: Permitir NULL inicialmente
    @Column(name = "payment_id", nullable = true, unique = true)
    private Long paymentId;

    // ✅ CAMBIO: Permitir NULL inicialmente
    @Column(nullable = true)
    private String status;

    @Column(name = "status_detail")
    private String statusDetail;

    @Column(name = "payment_method_id")
    private String paymentMethodId;

    @Column(name = "payment_type_id")
    private String paymentTypeId;

    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @Column(name = "date_approved")
    private LocalDateTime dateApproved;

    // CAMBIO: Ahora se relaciona con Pago en lugar de Factura
    @OneToOne
    @JoinColumn(name = "id_pago", nullable = false)
    private Pago pago;
}