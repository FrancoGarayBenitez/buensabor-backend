package com.elbuensabor.repository;

import com.elbuensabor.entities.Estado;
import com.elbuensabor.entities.Pedido;
import com.elbuensabor.entities.TipoEnvio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IPedidoRepository extends JpaRepository<Pedido, Long> {

        // ==================== CONSULTAS BÁSICAS ====================

        /**
         * Obtiene todos los pedidos ordenados por fecha descendente
         */
        List<Pedido> findAllByOrderByFechaDesc();

        /**
         * Busca pedidos por estado ordenados por fecha descendente
         */
        List<Pedido> findByEstadoOrderByFechaDesc(Estado estado);

        /**
         * Busca pedidos por múltiples estados ordenados por fecha ascendente
         */
        List<Pedido> findByEstadoInOrderByFechaAsc(List<Estado> estados);

        // ==================== CONSULTAS POR CLIENTE ====================

        /**
         * Obtiene pedidos de un cliente específico ordenados por fecha descendente
         */
        List<Pedido> findByCliente_IdClienteOrderByFechaDesc(Long idCliente);

        /**
         * Verifica si un pedido pertenece a un cliente
         */
        boolean existsByIdPedidoAndCliente_IdCliente(Long idPedido, Long idCliente);

        /**
         * Obtiene el último pedido de un cliente
         */
        Optional<Pedido> findFirstByCliente_IdClienteOrderByFechaDesc(Long idCliente);

        // ==================== CONSULTAS POR FECHA ====================

        /**
         * Busca pedidos entre dos fechas ordenados por fecha descendente
         */
        List<Pedido> findByFechaBetweenOrderByFechaDesc(LocalDateTime fechaInicio, LocalDateTime fechaFin);

        /**
         * Busca pedidos de un día específico
         */
        @Query("SELECT p FROM Pedido p WHERE DATE(p.fecha) = :fecha ORDER BY p.fecha DESC")
        List<Pedido> findByFechaDate(@Param("fecha") LocalDate fecha);

        /**
         * Obtiene pedidos del mes actual
         */
        @Query("SELECT p FROM Pedido p WHERE MONTH(p.fecha) = MONTH(CURRENT_DATE) " +
                        "AND YEAR(p.fecha) = YEAR(CURRENT_DATE) ORDER BY p.fecha DESC")
        List<Pedido> findPedidosMesActual();

        // ==================== CONSULTAS POR ESTADO Y TIPO ====================

        /**
         * Busca pedidos por estado y tipo de envío
         */
        List<Pedido> findByEstadoAndTipoEnvioOrderByFechaAsc(Estado estado, TipoEnvio tipoEnvio);

        /**
         * Obtiene pedidos pendientes de confirmación de pago
         */
        @Query("SELECT p FROM Pedido p WHERE p.formaPago = 'EFECTIVO' " +
                        "AND p.pagoConfirmado = false AND p.estado = 'PENDIENTE' " +
                        "ORDER BY p.fecha ASC")
        List<Pedido> findPedidosPendientesPago();

        /**
         * Obtiene pedidos listos para delivery sin delivery asignado
         */
        @Query("SELECT p FROM Pedido p WHERE p.estado = 'LISTO' " +
                        "AND p.tipoEnvio = 'DELIVERY' AND p.usuarioDelivery IS NULL " +
                        "ORDER BY p.fecha ASC")
        List<Pedido> findPedidosListosSinDelivery();

        // ==================== CONSULTAS PARA COCINA ====================

        // /**
        // * Obtiene pedidos en preparación o pendientes
        // */
        // @Query("SELECT p FROM Pedido p WHERE p.estado IN ('PENDIENTE', 'PREPARACION')
        // " +
        // "AND (p.formaPago = 'MERCADO_PAGO' OR p.pagoConfirmado = true) " +
        // "ORDER BY p.fecha ASC")
        // List<Pedido> findPedidosParaCocina();

        /**
         * Obtiene pedidos en preparación o pendientes que contienen
         * al menos un artículo manufacturado (necesita cocina).
         * Los pedidos con solo artículos insumo de venta directa (bebidas) son
         * excluidos.
         */
        @Query("SELECT DISTINCT p FROM Pedido p JOIN p.detalles d " +
                        "WHERE p.estado IN ('PENDIENTE', 'PREPARACION') " +
                        "AND (p.formaPago = 'MERCADO_PAGO' OR p.pagoConfirmado = true) " +
                        "AND TYPE(d.articulo) = ArticuloManufacturado " +
                        "ORDER BY p.fecha ASC")
        List<Pedido> findPedidosParaCocina();

        /**
         * Obtiene pedidos en preparación retrasados
         */
        @Query("SELECT p FROM Pedido p WHERE p.estado = 'PREPARACION' " +
                        "AND p.horaEstimadaFinalizacion < CURRENT_TIME " +
                        "ORDER BY p.fecha ASC")
        List<Pedido> findPedidosRetrasados();

        // ==================== CONSULTAS PARA DELIVERY ====================

        /**
         * Obtiene pedidos asignados a un delivery específico
         */
        List<Pedido> findByUsuarioDelivery_IdUsuarioAndEstadoOrderByFechaAsc(Long idUsuario, Estado estado);

        /**
         * Obtiene pedidos listos para un delivery específico
         */
        @Query("SELECT p FROM Pedido p WHERE p.usuarioDelivery.idUsuario = :idDelivery " +
                        "AND p.estado = 'LISTO' AND p.tipoEnvio = 'DELIVERY' ORDER BY p.fecha ASC")
        List<Pedido> findPedidosListosParaDelivery(@Param("idDelivery") Long idDelivery);

        /**
         * Obtiene pedidos listos para un delivery específico, ordenados por fecha
         * descendente
         */
        @Query("SELECT p FROM Pedido p WHERE p.usuarioDelivery.idUsuario = :idDelivery " +
                        "AND p.estado = 'LISTO' AND p.tipoEnvio = 'DELIVERY' ORDER BY p.fecha DESC")
        List<Pedido> findByUsuarioDelivery_IdUsuarioOrderByFechaDesc(@Param("idDelivery") Long idDelivery);

        // ==================== CONSULTAS ESTADÍSTICAS ====================

        /**
         * Cuenta pedidos por estado
         */
        Long countByEstado(Estado estado);

        /**
         * Cuenta pedidos de un cliente
         */
        Long countByCliente_IdCliente(Long idCliente);

        /**
         * Cuenta pedidos del día actual
         */
        @Query("SELECT COUNT(p) FROM Pedido p WHERE DATE(p.fecha) = CURRENT_DATE")
        Long countPedidosHoy();

        /**
         * Suma total de ventas del día
         */
        @Query("SELECT SUM(p.total) FROM Pedido p WHERE DATE(p.fecha) = CURRENT_DATE " +
                        "AND p.estado != 'CANCELADO'")
        Double sumTotalVentasHoy();

        /**
         * Suma total de ventas entre fechas
         */
        @Query("SELECT SUM(p.total) FROM Pedido p WHERE p.fecha BETWEEN :fechaInicio AND :fechaFin " +
                        "AND p.estado != 'CANCELADO'")
        Double sumTotalVentasEntreFechas(@Param("fechaInicio") LocalDateTime fechaInicio,
                        @Param("fechaFin") LocalDateTime fechaFin);

        /**
         * Obtiene los pedidos más recientes
         */
        List<Pedido> findTop10ByOrderByFechaDesc();

        // ==================== CONSULTAS DE VALIDACIÓN ====================

        /**
         * Verifica si existe un pedido pendiente para un cliente
         */
        @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Pedido p " +
                        "WHERE p.cliente.idCliente = :idCliente AND p.estado IN ('PENDIENTE', 'PREPARACION')")
        boolean existePedidoPendienteParaCliente(@Param("idCliente") Long idCliente);

        /**
         * Busca pedido por código de MercadoPago
         */
        Optional<Pedido> findByCodigoMercadoPago(String codigoMercadoPago);

        // ==================== CONSULTAS PARA REPORTES ====================

        /**
         * Obtiene pedidos agrupados por día en un rango de fechas
         */
        @Query("SELECT DATE(p.fecha) as fecha, COUNT(p) as cantidad, SUM(p.total) as total " +
                        "FROM Pedido p WHERE p.fecha BETWEEN :fechaInicio AND :fechaFin " +
                        "AND p.estado != 'CANCELADO' GROUP BY DATE(p.fecha) ORDER BY DATE(p.fecha) DESC")
        List<Object[]> findResumenVentasPorDia(@Param("fechaInicio") LocalDateTime fechaInicio,
                        @Param("fechaFin") LocalDateTime fechaFin);

        /**
         * Obtiene los productos más vendidos
         */
        @Query("SELECT a.denominacion, SUM(d.cantidad) as cantidad " +
                        "FROM DetallePedido d JOIN d.articulo a JOIN d.pedido p " +
                        "WHERE p.estado != 'CANCELADO' " +
                        "GROUP BY a.idArticulo, a.denominacion " +
                        "ORDER BY cantidad DESC")
        List<Object[]> findProductosMasVendidos();

        /**
         * Obtiene tiempo promedio de preparación
         */
        @Query("SELECT AVG(TIMESTAMPDIFF(MINUTE, p.fechaInicioPreparacion, p.fechaListo)) " +
                        "FROM Pedido p WHERE p.fechaInicioPreparacion IS NOT NULL " +
                        "AND p.fechaListo IS NOT NULL")
        Double findTiempoPromedioPreparacion();

        // ==================== CONSULTAS PARA CAJERO ====================

        /**
         * Obtiene pedidos del día por forma de pago
         */
        @Query("SELECT p FROM Pedido p WHERE DATE(p.fecha) = CURRENT_DATE " +
                        "AND p.formaPago = :formaPago ORDER BY p.fecha DESC")
        List<Pedido> findPedidosHoyPorFormaPago(@Param("formaPago") com.elbuensabor.entities.FormaPago formaPago);

        /**
         * Obtiene pedidos confirmados por un cajero específico
         */
        List<Pedido> findByUsuarioConfirmaPago_IdUsuarioOrderByFechaConfirmacionPagoDesc(Long idUsuario);
}