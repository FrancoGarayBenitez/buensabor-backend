package com.elbuensabor.repository;

import com.elbuensabor.entities.Pago;
import com.elbuensabor.entities.EstadoPago;
import com.elbuensabor.entities.FormaPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IPagoRepository extends JpaRepository<Pago, Long> {

    // Buscar pagos por factura
    List<Pago> findByFacturaIdFactura(Long facturaId);

    // Buscar pagos por estado
    List<Pago> findByEstado(EstadoPago estado);

    // Buscar pagos por forma de pago
    List<Pago> findByFormaPago(FormaPago formaPago);

    // Buscar pago por payment ID de Mercado Pago
    @Query("SELECT p FROM Pago p WHERE p.datosMercadoPago.paymentId = :paymentId AND p.datosMercadoPago.paymentId IS NOT NULL")
    Optional<Pago> findByMercadoPagoPaymentId(@Param("paymentId") Long paymentId);

    // Buscar pagos por rango de fechas
    List<Pago> findByFechaCreacionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Obtener total pagado para una factura
    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p WHERE p.factura.idFactura = :facturaId AND p.estado = 'APROBADO'")
    Double getTotalPagadoByFactura(@Param("facturaId") Long facturaId);

    // Buscar pagos pendientes
    @Query("SELECT p FROM Pago p WHERE p.estado = 'PENDIENTE' AND p.fechaCreacion < :fechaLimite")
    List<Pago> findPagosPendientesAntiguos(@Param("fechaLimite") LocalDateTime fechaLimite);

    // Buscar pagos por cliente (a través de pedido -> factura)
    @Query("SELECT p FROM Pago p WHERE p.factura.pedido.cliente.idCliente = :clienteId")
    List<Pago> findByClienteId(@Param("clienteId") Long clienteId);

    @Query("SELECT p FROM Pago p WHERE p.mercadoPagoPreferenceId = :preferenceId")
    Optional<Pago> findByMercadoPagoPreferenceId(@Param("preferenceId") String preferenceId);
}