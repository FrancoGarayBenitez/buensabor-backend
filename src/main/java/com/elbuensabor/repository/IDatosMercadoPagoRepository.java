package com.elbuensabor.repository;

import com.elbuensabor.entities.DatosMercadoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IDatosMercadoPagoRepository extends JpaRepository<DatosMercadoPago, Long> {

    // Buscar por payment ID de MercadoPago (puede ser null inicialmente)
    Optional<DatosMercadoPago> findByPaymentId(Long paymentId);

    // Buscar registros donde payment_id es null (pendientes de webhook)
    List<DatosMercadoPago> findByPaymentIdIsNull();

    // Buscar por pago
    Optional<DatosMercadoPago> findByPagoIdPago(Long pagoId);

    // Buscar por estado
    List<DatosMercadoPago> findByStatus(String status);

    // Buscar por status detail
    List<DatosMercadoPago> findByStatusDetail(String statusDetail);

    // Buscar por método de pago
    List<DatosMercadoPago> findByPaymentMethodId(String paymentMethodId);

    // Buscar por rango de fechas de creación
    List<DatosMercadoPago> findByDateCreatedBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Buscar por rango de fechas de aprobación
    List<DatosMercadoPago> findByDateApprovedBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Buscar pagos aprobados en un período
    @Query("SELECT d FROM DatosMercadoPago d WHERE d.status = 'approved' AND d.dateApproved BETWEEN :fechaInicio AND :fechaFin")
    List<DatosMercadoPago> findPagosAprobadosEnPeriodo(@Param("fechaInicio") LocalDateTime fechaInicio,
                                                       @Param("fechaFin") LocalDateTime fechaFin);

    // Obtener estadísticas por método de pago
    @Query("SELECT d.paymentMethodId, COUNT(d) FROM DatosMercadoPago d WHERE d.status = 'approved' GROUP BY d.paymentMethodId")
    List<Object[]> getEstadisticasPorMetodoPago();

    // Buscar datos MP por factura (a través de pago)
    @Query("SELECT d FROM DatosMercadoPago d WHERE d.pago.factura.idFactura = :facturaId")
    List<DatosMercadoPago> findByFacturaId(@Param("facturaId") Long facturaId);
}