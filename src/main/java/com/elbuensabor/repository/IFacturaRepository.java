package com.elbuensabor.repository;

import com.elbuensabor.entities.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IFacturaRepository extends JpaRepository<Factura, Long> {

    // Buscar factura por número de comprobante
    Optional<Factura> findByNroComprobante(String nroComprobante);

    // Buscar facturas por pedido
    Optional<Factura> findByPedidoIdPedido(Long pedidoId);

    // Buscar facturas por rango de fechas
    List<Factura> findByFechaFacturaBetween(LocalDate fechaInicio, LocalDate fechaFin);

    // Buscar facturas por cliente (a través del pedido)
    @Query("SELECT f FROM Factura f WHERE f.pedido.cliente.idCliente = :clienteId")
    List<Factura> findByClienteId(@Param("clienteId") Long clienteId);

    // Obtener facturas pendientes de pago completo
    @Query("SELECT f FROM Factura f WHERE f.totalVenta > " +
            "(SELECT COALESCE(SUM(p.monto), 0) FROM Pago p WHERE p.factura.idFactura = f.idFactura AND p.estado = 'APROBADO')")
    List<Factura> findFacturasPendientesPago();

    // Verificar si existe factura para un pedido
    boolean existsByPedidoIdPedido(Long pedidoId);

    // Obtener total de ventas por período
    @Query("SELECT COALESCE(SUM(f.totalVenta), 0) FROM Factura f WHERE f.fechaFactura BETWEEN :fechaInicio AND :fechaFin")
    Double getTotalVentasByPeriodo(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);
}