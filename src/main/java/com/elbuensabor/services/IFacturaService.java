package com.elbuensabor.services;

import com.elbuensabor.dto.response.FacturaResponseDTO;
import com.elbuensabor.entities.Factura;
import com.elbuensabor.entities.Pedido;

import java.time.LocalDate;
import java.util.List;

public interface IFacturaService extends IGenericService<Factura, Long, FacturaResponseDTO> {

    // Crear factura automáticamente desde un pedido
    FacturaResponseDTO crearFacturaFromPedido(Pedido pedido);

    // Buscar factura por pedido
    FacturaResponseDTO findByPedidoId(Long pedidoId);

    // Buscar facturas por cliente
    List<FacturaResponseDTO> findByClienteId(Long clienteId);

    // Buscar facturas por rango de fechas
    List<FacturaResponseDTO> findByFechaRange(LocalDate fechaInicio, LocalDate fechaFin);

    // Obtener facturas pendientes de pago
    List<FacturaResponseDTO> findFacturasPendientesPago();

    // Verificar si pedido ya tiene factura
    boolean existeFacturaParaPedido(Long pedidoId);

    // Generar número de comprobante único
    String generarNumeroComprobante();

    // Calcular totales de factura
    FacturaResponseDTO calcularTotales(Pedido pedido);
}